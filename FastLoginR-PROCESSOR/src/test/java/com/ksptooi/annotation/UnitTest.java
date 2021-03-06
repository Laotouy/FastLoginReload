package com.ksptooi.annotation;

import com.ksptooi.flr.input.annotation.ProcessMapper;
import com.ksptooi.flr.input.processor.PlayerAccountProcessor;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class UnitTest {

    @Test
    public void test() throws InvocationTargetException, IllegalAccessException, NoSuchMethodException, InstantiationException {

        Class c = PlayerAccountProcessor.class;


        Object obj1 = c.newInstance();

        Object obj = c.getConstructor(new Class[] {}).newInstance(new Object[] {});

        Method[] declaredMethods = c.getDeclaredMethods();

        for(Method m:declaredMethods){

            ProcessMapper annotation = m.getAnnotation(ProcessMapper.class);

            String cmdName = null;

            if(annotation!=null){
                cmdName = annotation.value();
                m.invoke(obj1,"");
            }

            System.out.println(annotation);

        }


    }


}
