package org.bricolages.streaming.filter;
import lombok.*;

class RenameOp extends Op {
    static final void register() {
        Op.registerOperator("rename", (def) ->
            new RenameOp(def, def.mapParameters(Parameters.class))
        );
    }

    @Getter
    @Setter
    static class Parameters {
        String to;
    }

    final String to;

    RenameOp(OperatorDefinition def, Parameters params) {
        this(def, params.to);
    }

    RenameOp(OperatorDefinition def, String to) {
        super(def);
        this.to = to;
    }

    @Override
    public Record apply(Record record) {
        String from = getColumnName();
        Object value = record.get(from);
        record.remove(from);
        if (value != null) {
            record.put(to, value);
        }
        return (record.isEmpty() ? null : record);
    }
}
