package org.yiwan.webcore.web;

import org.yiwan.webcore.database.DatabaseWrapper;

import java.lang.reflect.Constructor;

public class PageFactory {
    private IWebDriverWrapper webDriverWrapper;
    private DatabaseWrapper databaseWrapper;

    public PageFactory(IWebDriverWrapper webDriverWrapper) {
        this.webDriverWrapper = webDriverWrapper;
    }

    public PageFactory(DatabaseWrapper databaseWrapper) {
        this.databaseWrapper = databaseWrapper;
    }

    public PageFactory(IWebDriverWrapper webDriverWrapper, DatabaseWrapper databaseWrapper) {
        this.webDriverWrapper = webDriverWrapper;
        this.databaseWrapper = databaseWrapper;
    }

    public <T extends PageBase> T createByWebDriverWrapper(Class<T> clazz) throws Exception {
        Constructor<T> c = clazz.getDeclaredConstructor(IWebDriverWrapper.class);
        c.setAccessible(true);
        return c.newInstance(webDriverWrapper);
    }

    public <T extends PageBase> T createByDatabaseWrapper(Class<T> clazz) throws Exception {
        Constructor<T> c = clazz.getDeclaredConstructor(DatabaseWrapper.class);
        c.setAccessible(true);
        return c.newInstance(databaseWrapper);
    }

    public <T extends PageBase> T createByWebDriverWrapperAndDatabaseWrapper(Class<T> clazz) throws Exception {
        Constructor<T> c = clazz.getDeclaredConstructor(IWebDriverWrapper.class, DatabaseWrapper.class);
        c.setAccessible(true);
        return c.newInstance(webDriverWrapper, databaseWrapper);
    }
}
