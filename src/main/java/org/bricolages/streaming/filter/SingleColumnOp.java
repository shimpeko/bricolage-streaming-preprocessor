package org.bricolages.streaming.filter;
import lombok.*;

public abstract class SingleColumnOp extends Op {
    protected SingleColumnOp(OperatorDefinition def) {
        super(def);
    }

    protected String targetColumnName() {
        return getColumnName();
    }

    @Override
    public Record apply(Record record) {
        Object result;
        try {
            result = applyValue(record.get(targetColumnName()), record);
        }
        catch (FilterException ex) {
            result = null;
        }
        if (result == null) {
            record.remove(targetColumnName());
            return record.isEmpty() ? null : record;
        }
        else {
            record.put(targetColumnName(), result);
            return record;
        }
    }

    protected abstract Object applyValue(Object value, Record record) throws FilterException;
}
