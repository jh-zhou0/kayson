package cn.zjh.kayson.framework.mybatis.core.type;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;
import org.apache.ibatis.type.TypeHandler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * List<String> 的类型转换器实现类，对应数据库的 varchar 类型
 * 
 * @author zjh - kayson
 */
@MappedJdbcTypes(JdbcType.VARCHAR)
@MappedTypes(List.class)
public class StringListTypeHandler implements TypeHandler<List<String>> {

    private static final String COMMA = ",";
    
    @Override
    public void setParameter(PreparedStatement ps, int i, List<String> parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(i, CollUtil.join(parameter, COMMA));
    }

    @Override
    public List<String> getResult(ResultSet rs, String columnName) throws SQLException {
        return getResult(rs.getString(columnName));
    }

    @Override
    public List<String> getResult(ResultSet rs, int columnIndex) throws SQLException {
        return getResult(rs.getString(columnIndex));
    }

    @Override
    public List<String> getResult(CallableStatement cs, int columnIndex) throws SQLException {
        return getResult(cs.getString(columnIndex));
    }
    
    private static List<String> getResult(String value) {
        if (value == null) {
            return null;
        }
        return StrUtil.splitTrim(value, COMMA);
    }
}
