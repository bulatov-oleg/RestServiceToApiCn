package ru.dmv.restServiceToApiCn.TestUtils;

import java.lang.reflect.Method;


public class InvokerProtectedMethod {

    public Object invokeProtectedMethod(Object test, String methodName, Object params[]) throws Exception {
        Object ret = null;

        final Method[] methods =
                test.getClass().getDeclaredMethods();

        for (int i = 0; i < methods.length; ++i) {
            if (methods[i].getName().equals(methodName)) {
                methods[i].setAccessible(true);
                ret = methods[i].invoke(test, params);
                break;
            }
        }

        return ret;
    }

    /*Не работает с private методами, только с protected
    Пример использования:
    MyClass instance = new MyClass();
    String expResult = "Expected Result";
    Object[] params = {"A String Value", "Another Value"};
    String result = (String) this.invokeProtectedMethod(instance, "myPrivateName", params);
    assertEquals(expResult, result);*/
}
