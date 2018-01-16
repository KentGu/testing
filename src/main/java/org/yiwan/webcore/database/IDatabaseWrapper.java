package org.yiwan.webcore.database;

import org.assertj.core.api.AbstractBooleanAssert;
import org.assertj.core.api.AbstractCharSequenceAssert;

import java.util.HashMap;
import java.util.List;

/**
 * Created by Kent Gu on 7/7/2017.
 */
public interface IDatabaseWrapper {

    public interface IFluentDatabaseAssertion {
        public AbstractCharSequenceAssert<?, String> columnValue(String key);

        public AbstractBooleanAssert<?> rowExisted(List<HashMap<String, String>> actual);

        public AbstractBooleanAssert<?> rowDistinctExisted(List<HashMap<String, String>> actual);
    }
}
