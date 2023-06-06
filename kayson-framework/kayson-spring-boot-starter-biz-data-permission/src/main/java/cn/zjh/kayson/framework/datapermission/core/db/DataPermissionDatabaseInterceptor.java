package cn.zjh.kayson.framework.datapermission.core.db;

import cn.hutool.core.collection.CollUtil;
import cn.zjh.kayson.framework.common.util.collection.SetUtils;
import cn.zjh.kayson.framework.datapermission.core.rule.DataPermissionRule;
import cn.zjh.kayson.framework.datapermission.core.rule.DataPermissionRuleFactory;
import cn.zjh.kayson.framework.mybatis.core.util.MyBatisUtils;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.PluginUtils;
import com.baomidou.mybatisplus.extension.parser.JsqlParserSupport;
import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.ExistsExpression;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.select.*;
import net.sf.jsqlparser.statement.update.Update;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.sql.Connection;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 数据权限拦截器，通过 {@link DataPermissionRule} 数据权限规则，重写 SQL 的方式来实现
 * 主要的 SQL 重写方法，可见 {@link #builderExpression(Expression, List)} 方法
 * 
 * 整体的代码实现上，参考 {@link com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor} 实现。
 * 所以每次 MyBatis Plus 升级时，需要 Review 下其具体的实现是否有变更！
 * 
 * @author zjh - kayson
 */
@RequiredArgsConstructor
public class DataPermissionDatabaseInterceptor extends JsqlParserSupport implements InnerInterceptor {
    
    private final DataPermissionRuleFactory ruleFactory;
    
    @Getter
    private final MappedStatementCache mappedStatementCache = new MappedStatementCache();

    // 处理 SELECT 场景
    @Override
    public void beforeQuery(Executor executor, MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) {
        // 获得 Mapper 对应的数据权限的规则
        List<DataPermissionRule> rules = ruleFactory.getDataPermissionRule(ms.getId());
        // 判断是否需要重写，如果无需重写，则跳过
        if (mappedStatementCache.nonRewritable(ms, rules)) {
            return;
        }
        PluginUtils.MPBoundSql mpBs = PluginUtils.mpBoundSql(boundSql);
        try {
            // 初始化上下文
            DataPermissionRuleContextHolder.init(rules);
            // 处理 SQL
            mpBs.sql(parserSingle(mpBs.sql(), null));
        } finally {
            // 添加到是否需重写缓存
            addMappedStatementCache(ms);
            // 清空上下文
            DataPermissionRuleContextHolder.clear();
        }
    }
    
    // 只处理 UPDATE / DELETE 场景，不处理 INSERT 场景（因为 INSERT 不需要数据权限)
    @Override
    public void beforePrepare(StatementHandler sh, Connection connection, Integer transactionTimeout) {
        PluginUtils.MPStatementHandler mpSh = PluginUtils.mpStatementHandler(sh);
        MappedStatement ms = mpSh.mappedStatement();
        SqlCommandType sct = ms.getSqlCommandType();
        if (sct == SqlCommandType.UPDATE || sct == SqlCommandType.DELETE) {
            // 获得 Mapper 对应的数据权限的规则
            List<DataPermissionRule> rules = ruleFactory.getDataPermissionRule(ms.getId());
            // 判断是否需要重写，如果无需重写，则跳过
            if (mappedStatementCache.nonRewritable(ms, rules)) {
                return;
            }
            PluginUtils.MPBoundSql mpBs = mpSh.mPBoundSql();
            try {
                // 初始化上下文
                DataPermissionRuleContextHolder.init(rules);
                // 处理 SQL
                mpBs.sql(parserMulti(mpBs.sql(), null));
            } finally {
                // 添加到是否需重写缓存
                mappedStatementCache.addNonRewritable(ms, rules);
                // 清空上下文
                DataPermissionRuleContextHolder.clear();
            }
        }
    }

    // SELECT 语句处理
    @Override
    protected void processSelect(Select select, int index, String sql, Object obj) {
        processSelectBody(select.getSelectBody());
        List<WithItem> withItemsList = select.getWithItemsList();
        if (!CollectionUtils.isEmpty(withItemsList)) {
            withItemsList.forEach(this::processSelectBody);
        }
    }

    // UPDATE 语句处理
    @Override
    protected void processUpdate(Update update, int index, String sql, Object obj) {
        final Table table = update.getTable();
        update.setWhere(this.builderExpression(update.getWhere(), table));
    }

    // DELETE 语句处理
    @Override
    protected void processDelete(Delete delete, int index, String sql, Object obj) {
        final Table table = delete.getTable();
        delete.setWhere(this.builderExpression(delete.getWhere(), table));
    }

    // ========== 和 TenantLineInnerInterceptor 一致的逻辑 ==========
    
    protected void processSelectBody(SelectBody selectBody) {
        if (selectBody == null) {
            return;
        }
        if (selectBody instanceof PlainSelect) {
            processPlainSelect((PlainSelect) selectBody);
        } else if (selectBody instanceof WithItem) {
            WithItem withItem = (WithItem) selectBody;
            processSelectBody(withItem.getSubSelect().getSelectBody());
        } else {
            SetOperationList operationList = (SetOperationList) selectBody;
            List<SelectBody> selectBodyList = operationList.getSelects();
            if (CollectionUtils.isNotEmpty(selectBodyList)) {
                selectBodyList.forEach(this::processSelectBody);
            }
        }
    }

    /**
     * 处理 PlainSelect
     */
    protected void processPlainSelect(final PlainSelect plainSelect) {
        //#3087 github
        List<SelectItem> selectItems = plainSelect.getSelectItems();
        if (CollectionUtils.isNotEmpty(selectItems)) {
            selectItems.forEach(this::processSelectItem);
        }

        // 处理 where 中的子查询
        Expression where = plainSelect.getWhere();
        processWhereSubSelect(where);

        // 处理 fromItem
        FromItem fromItem = plainSelect.getFromItem();
        List<Table> list = processFromItem(fromItem);
        List<Table> mainTables = new ArrayList<>(list);

        // 处理 join
        List<Join> joins = plainSelect.getJoins();
        if (CollectionUtils.isNotEmpty(joins)) {
            mainTables = processJoins(mainTables, joins);
        }

        // 当有 mainTable 时，进行 where 条件追加
        if (CollectionUtils.isNotEmpty(mainTables)) {
            plainSelect.setWhere(builderExpression(where, mainTables));
        }
    }

    protected void processSelectItem(SelectItem selectItem) {
        if (selectItem instanceof SelectExpressionItem) {
            SelectExpressionItem selectExpressionItem = (SelectExpressionItem) selectItem;
            final Expression expression = selectExpressionItem.getExpression();
            if (expression instanceof SubSelect) {
                processSelectBody(((SubSelect) expression).getSelectBody());
            } else if (expression instanceof Function) {
                processFunction((Function) expression);
            }
        }
    }

    /**
     * 处理where条件内的子查询
     * <p>
     * 支持如下:
     * 1. in
     * 2. =
     * 3. >
     * 4. <
     * 5. >=
     * 6. <=
     * 7. <>
     * 8. EXISTS
     * 9. NOT EXISTS
     * <p>
     * 前提条件:
     * 1. 子查询必须放在小括号中
     * 2. 子查询一般放在比较操作符的右边
     *
     * @param where where 条件
     */
    protected void processWhereSubSelect(Expression where) {
        if (where == null) {
            return;
        }
        if (where instanceof FromItem) {
            processOtherFromItem((FromItem) where);
            return;
        }
        if (where.toString().indexOf("SELECT") > 0) {
            // 有子查询
            if (where instanceof BinaryExpression) {
                // 比较符号 , and , or , 等等
                BinaryExpression expression = (BinaryExpression) where;
                processWhereSubSelect(expression.getLeftExpression());
                processWhereSubSelect(expression.getRightExpression());
            } else if (where instanceof InExpression) {
                // in
                InExpression expression = (InExpression) where;
                Expression inExpression = expression.getRightExpression();
                if (inExpression instanceof SubSelect) {
                    processSelectBody(((SubSelect) inExpression).getSelectBody());
                }
            } else if (where instanceof ExistsExpression) {
                // exists
                ExistsExpression expression = (ExistsExpression) where;
                processWhereSubSelect(expression.getRightExpression());
            } else if (where instanceof NotExpression) {
                // not exists
                NotExpression expression = (NotExpression) where;
                processWhereSubSelect(expression.getExpression());
            } else if (where instanceof Parenthesis) {
                Parenthesis expression = (Parenthesis) where;
                processWhereSubSelect(expression.getExpression());
            }
        }
    }

    /**
     * 处理子查询等
     */
    protected void processOtherFromItem(FromItem fromItem) {
        // 去除括号
        while (fromItem instanceof ParenthesisFromItem) {
            fromItem = ((ParenthesisFromItem) fromItem).getFromItem();
        }

        if (fromItem instanceof SubSelect) {
            SubSelect subSelect = (SubSelect) fromItem;
            if (subSelect.getSelectBody() != null) {
                processSelectBody(subSelect.getSelectBody());
            }
        } else if (fromItem instanceof ValuesList) {
            logger.debug("Perform a subQuery, if you do not give us feedback");
        } else if (fromItem instanceof LateralSubSelect) {
            LateralSubSelect lateralSubSelect = (LateralSubSelect) fromItem;
            if (lateralSubSelect.getSubSelect() != null) {
                SubSelect subSelect = lateralSubSelect.getSubSelect();
                if (subSelect.getSelectBody() != null) {
                    processSelectBody(subSelect.getSelectBody());
                }
            }
        }
    }

    /**
     * 处理 sub join
     *
     * @param subJoin subJoin
     * @return Table subJoin 中的主表
     */
    private List<Table> processSubJoin(SubJoin subJoin) {
        List<Table> mainTables = new ArrayList<>();
        if (subJoin.getJoinList() != null) {
            List<Table> list = processFromItem(subJoin.getLeft());
            mainTables.addAll(list);
            mainTables = processJoins(mainTables, subJoin.getJoinList());
        }
        return mainTables;
    }

    private List<Table> processFromItem(FromItem fromItem) {
        // 处理括号括起来的表达式
        while (fromItem instanceof ParenthesisFromItem) {
            fromItem = ((ParenthesisFromItem) fromItem).getFromItem();
        }

        List<Table> mainTables = new ArrayList<>();
        // 无 join 时的处理逻辑
        if (fromItem instanceof Table) {
            Table fromTable = (Table) fromItem;
            mainTables.add(fromTable);
        } else if (fromItem instanceof SubJoin) {
            // SubJoin 类型则还需要添加上 where 条件
            List<Table> tables = processSubJoin((SubJoin) fromItem);
            mainTables.addAll(tables);
        } else {
            // 处理下 fromItem
            processOtherFromItem(fromItem);
        }
        return mainTables;
    }

    /**
     * 处理 joins
     *
     * @param mainTables 可以为 null
     * @param joins      join 集合
     * @return List<Table> 右连接查询的 Table 列表
     */
    private List<Table> processJoins(List<Table> mainTables, List<Join> joins) {
        // join 表达式中最终的主表
        Table mainTable = null;
        // 当前 join 的左表
        Table leftTable = null;

        if (mainTables == null) {
            mainTables = new ArrayList<>();
        } else if (mainTables.size() == 1) {
            mainTable = mainTables.get(0);
            leftTable = mainTable;
        }

        //对于 on 表达式写在最后的 join，需要记录下前面多个 on 的表名
        Deque<List<Table>> onTableDeque = new LinkedList<>();
        for (Join join : joins) {
            // 处理 on 表达式
            FromItem joinItem = join.getRightItem();

            // 获取当前 join 的表，subJoint 可以看作是一张表
            List<Table> joinTables = null;
            if (joinItem instanceof Table) {
                joinTables = new ArrayList<>();
                joinTables.add((Table) joinItem);
            } else if (joinItem instanceof SubJoin) {
                joinTables = processSubJoin((SubJoin) joinItem);
            }

            if (joinTables != null) {

                // 如果是隐式内连接
                if (join.isSimple()) {
                    mainTables.addAll(joinTables);
                    continue;
                }

                // 当前表是否忽略
                Table joinTable = joinTables.get(0);

                List<Table> onTables = null;
                // 如果不要忽略，且是右连接，则记录下当前表
                if (join.isRight()) {
                    mainTable = joinTable;
                    if (leftTable != null) {
                        onTables = Collections.singletonList(leftTable);
                    }
                } else if (join.isLeft()) {
                    onTables = Collections.singletonList(joinTable);
                } else if (join.isInner()) {
                    if (mainTable == null) {
                        onTables = Collections.singletonList(joinTable);
                    } else {
                        onTables = Arrays.asList(mainTable, joinTable);
                    }
                    mainTable = null;
                }

                mainTables = new ArrayList<>();
                if (mainTable != null) {
                    mainTables.add(mainTable);
                }

                // 获取 join 尾缀的 on 表达式列表
                Collection<Expression> originOnExpressions = join.getOnExpressions();
                // 正常 join on 表达式只有一个，立刻处理
                if (originOnExpressions.size() == 1 && onTables != null) {
                    List<Expression> onExpressions = new LinkedList<>();
                    onExpressions.add(builderExpression(originOnExpressions.iterator().next(), onTables));
                    join.setOnExpressions(onExpressions);
                    leftTable = joinTable;
                    continue;
                }
                // 表名压栈，忽略的表压入 null，以便后续不处理
                onTableDeque.push(onTables);
                // 尾缀多个 on 表达式的时候统一处理
                if (originOnExpressions.size() > 1) {
                    Collection<Expression> onExpressions = new LinkedList<>();
                    for (Expression originOnExpression : originOnExpressions) {
                        List<Table> currentTableList = onTableDeque.poll();
                        if (CollectionUtils.isEmpty(currentTableList)) {
                            onExpressions.add(originOnExpression);
                        } else {
                            onExpressions.add(builderExpression(originOnExpression, currentTableList));
                        }
                    }
                    join.setOnExpressions(onExpressions);
                }
                leftTable = joinTable;
            } else {
                processOtherFromItem(joinItem);
                leftTable = null;
            }
        }

        return mainTables;
    }

    /**
     * 处理函数
     */
    protected void processFunction(Function function) {
        ExpressionList parameters = function.getParameters();
        if (parameters != null) {
            parameters.getExpressions().forEach(expression -> {
                if (expression instanceof SubSelect) {
                    processSelectBody(((SubSelect) expression).getSelectBody());
                } else if (expression instanceof Function) {
                    processFunction((Function) expression);
                }
            });
        }
    }

    // ========== 和 TenantLineInnerInterceptor 存在差异的逻辑：关键，实现权限条件的拼接 ==========
    /**
     * 处理条件
     *
     * @param currentExpression 当前 where 条件
     * @param table             单个表
     */
    protected Expression builderExpression(Expression currentExpression, Table table) {
        return this.builderExpression(currentExpression, Collections.singletonList(table));
    }

    /**
     * 处理条件
     *
     * @param currentExpression 当前 where 条件
     * @param tables            多个表
     */
    protected Expression builderExpression(Expression currentExpression, List<Table> tables) {
        // 没有表需要处理，直接返回
        if (CollUtil.isEmpty(tables)) {
            return currentExpression;
        }
        // 1 获得 Table 对应的数据权限条件
        Expression dataPermissionExpression = null;
        for (Table table : tables) {
            // 构建每个表的权限 Expression 条件
            Expression expression = buildDataPermissionExpression(table);
            if (expression == null) {
                continue;
            }
            // 合并到 dataPermissionExpression 中
            dataPermissionExpression = dataPermissionExpression == null ? 
                    expression : new AndExpression(dataPermissionExpression, expression);
        }
        // 2 合并多个 Expression 条件
        if (dataPermissionExpression == null) {
            return currentExpression;
        }
        if (currentExpression == null) {
            return dataPermissionExpression;
        }
        if (currentExpression instanceof OrExpression) {
            return new AndExpression(new Parenthesis(currentExpression), dataPermissionExpression);
        }
        return new AndExpression(currentExpression, dataPermissionExpression);
    }

    /**
     * 构建指定表的数据权限的 Expression 过滤条件
     * 
     * @param table 表
     * @return 过滤条件
     */
    private Expression buildDataPermissionExpression(Table table) {
        Expression allExpression = null;
        for (DataPermissionRule rule : DataPermissionRuleContextHolder.getRules()) {
            // 判断表名是否匹配
            if (!CollUtil.contains(rule.getTableNames(), table.getName())) {
                continue;
            }
            // 如果有匹配的规则，说明可重写。
            // 为什么不是有 allExpression 非空才重写呢？在生成 column = value 过滤条件时，会因为 value 不存在，导致未重写。
            // 这样导致第一次无 value，被标记成无需重写；但是第二次有 value，此时会需要重写。
            DataPermissionRuleContextHolder.setRewrite(true);
            
            // 单条规则条件
            String tableName = MyBatisUtils.getTableName(table);
            Expression oneExpression = rule.getExpression(tableName, table.getAlias());
            // 拼接到 allExpression 中
            allExpression = allExpression == null ? oneExpression : new AndExpression(allExpression, oneExpression);
        }
        return allExpression;
    }

    /**
     * SQL 解析上下文，方便透传 {@link DataPermissionRule} 规则
     */
    static final class DataPermissionRuleContextHolder {

        /**
         * 该 {@link MappedStatement} 对应的规则
         */
        public static final ThreadLocal<List<DataPermissionRule>> RULES = ThreadLocal.withInitial(Collections::emptyList);

        /**
         * SQL 是否进行重写
         */
        public static final ThreadLocal<Boolean> REWRITE = ThreadLocal.withInitial(() -> Boolean.FALSE);
        
        public static void init(List<DataPermissionRule> rules) {
            RULES.set(rules);
            REWRITE.set(false);
        }
        
        public static List<DataPermissionRule> getRules() {
            return RULES.get();
        }
        
        public static boolean getRewrite() {
            return REWRITE.get();
        }
        
        public static void setRewrite(boolean rewrite) {
            REWRITE.set(rewrite);
        }
        
        public static void clear() {
            RULES.remove();
            REWRITE.remove();
        }
        
    }

    /**
     * 添加到是否需要重写缓存，如果需要重写，则不添加
     * 
     * @param ms MappedStatement
     */
    private void addMappedStatementCache(MappedStatement ms) {
        if (DataPermissionRuleContextHolder.getRewrite()) {
            return;
        }
        // 不需要重写，添加到缓存
        mappedStatementCache.addNonRewritable(ms, DataPermissionRuleContextHolder.getRules());
    }

    /**
     * {@link MappedStatement} 的缓存
     * 
     * 记录 {@link DataPermissionRule} 对指定 {@link MappedStatement} 是否有效
     * 如果无效，则避免 SQL 解析，加快速度
     */
    static final class MappedStatementCache {

        /**
         * 指定数据权限规则，对指定 MappedStatement 无需重写（不生效)的缓存
         * 
         * key:   数据权限规则
         * value: {@link MappedStatement#getId()} 编号
         */
        @Getter
        private final Map<Class<? extends DataPermissionRule>, Set<String>> nonRewritableMappedStatements = new ConcurrentHashMap<>();

        /**
         * 判断是否无须重写
         * 
         * @param ms MappedStatement
         * @param rules 数据权限规则集合
         * @return 是否无需重写
         */
        public boolean nonRewritable(MappedStatement ms, List<DataPermissionRule> rules) {
            // 如果没有数据权限规则，无需重写
            if (CollUtil.isEmpty(rules)) {
                return true;
            }
            // 任意 rules 在 nonRewritableMappedStatements 中不包含 ms，无需重写
            for (DataPermissionRule rule : rules) {
                Set<String> mappedStatementIds = nonRewritableMappedStatements.get(rule.getClass());
                if (!CollUtil.contains(mappedStatementIds, ms.getId())) {
                    return false;
                }
            }
            return true;
        }

        /**
         * 添加无需重写的 MappedStatement
         * 
         * @param ms MappedStatement
         * @param rules 数据权限规则集合
         */
        public void addNonRewritable(MappedStatement ms, List<DataPermissionRule> rules) {
            for (DataPermissionRule rule : rules) {
                Set<String> mappedStatementIds = nonRewritableMappedStatements.get(rule.getClass());
                if (CollUtil.isNotEmpty(mappedStatementIds)) {
                    mappedStatementIds.add(ms.getId());
                } else {
                    nonRewritableMappedStatements.put(rule.getClass(), SetUtils.asSet(ms.getId()));
                }
            }
        }

        /**
         * 清空缓存
         */
        public void clear() {
            nonRewritableMappedStatements.clear();
        }
        
    }
    
}
