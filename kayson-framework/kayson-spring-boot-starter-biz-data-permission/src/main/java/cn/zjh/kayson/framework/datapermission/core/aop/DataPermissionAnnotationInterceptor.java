package cn.zjh.kayson.framework.datapermission.core.aop;

import cn.zjh.kayson.framework.datapermission.core.annotation.DataPermission;
import lombok.Getter;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.core.MethodClassKey;
import org.springframework.core.annotation.AnnotationUtils;

import javax.annotation.Nonnull;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * {@link DataPermission} 注解拦截器
 * 
 * @author zjh - kayson
 */
@DataPermission // 该注解，用于 DATA_PERMISSION_NULL 的空对象
public class DataPermissionAnnotationInterceptor implements MethodInterceptor {

    /**
     * DataPermission 空对象，用于方法无 {@link DataPermission} 注解时，使用 DATA_PERMISSION_NULL 进行占位
     */
    private static final DataPermission DATA_PERMISSION_NULL = DataPermissionAnnotationInterceptor.class.getAnnotation(DataPermission.class);
    
    @Getter
    private final Map<MethodClassKey, DataPermission> dataPermissionCache = new ConcurrentHashMap<>();
    
    @Override
    public Object invoke(@Nonnull MethodInvocation invocation) throws Throwable {
        // 入栈
        DataPermission dataPermission = this.findAnnotation(invocation);
        if (dataPermission != null) {
            DataPermissionContextHolder.add(dataPermission);
        }
        try {
            // 执行逻辑
            return invocation.proceed();
        } finally {
            // 出栈
            if (dataPermission != null) {
                DataPermissionContextHolder.remove();
            }
        }
    }

    private DataPermission findAnnotation(MethodInvocation invocation) {
        Method method = invocation.getMethod();
        Object targetObject = invocation.getThis();
        Class<?> clazz = targetObject != null ? targetObject.getClass() : method.getDeclaringClass();
        MethodClassKey methodClassKey = new MethodClassKey(method, clazz);
        // 1 从缓存中获取
        DataPermission dataPermission = dataPermissionCache.get(methodClassKey);
        if (dataPermission != null) {
            return dataPermission != DATA_PERMISSION_NULL ? dataPermission : null;
        }
        // 2.1 从方法上获取
        dataPermission = AnnotationUtils.getAnnotation(method, DataPermission.class);
        // 2.2 从类上获取
        if (dataPermission == null) {
            dataPermission = AnnotationUtils.findAnnotation(clazz, DataPermission.class);
        }
        // 2.3 添加到缓存中
        dataPermissionCache.put(methodClassKey, dataPermission != null ? dataPermission : DATA_PERMISSION_NULL);
        return dataPermission;
    }


}
