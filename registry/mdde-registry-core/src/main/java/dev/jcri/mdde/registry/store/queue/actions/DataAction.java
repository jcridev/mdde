package dev.jcri.mdde.registry.store.queue.actions;

import java.util.UUID;

public abstract class DataAction {
    private final EActionType tupleAction;
    private UUID actionId;

    public DataAction(EActionType type){
        if(type == null){
            throw new IllegalArgumentException("Data action type can't be null");
        }
        actionId = UUID.randomUUID();
        tupleAction = type;
    }

    public EActionType getActionType() {
        return tupleAction;
    }

    /**
     * A unique ID generated for each action placed into the queue.
     * In case it needs to be identified specifically later
     * @return
     */
    public UUID getActionId() {
        return actionId;
    }

    protected void setActionId(UUID actionId) {
        this.actionId = actionId;
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof DataAction)){
            return false;
        }
        DataAction objCasted = (DataAction)obj;
        return objCasted.actionId == this.actionId;
    }

    /**
     * Type of the actions that can be placed into the queue for later execution against the data store
     */
    public enum EActionType{
        DELETE("del", (byte)0),
        COPY("copy", (byte)1);

        private String _actionTag;
        private byte _actionCode;

        EActionType(String tag, byte code){
            _actionTag = tag;
            _actionCode = code;
        }

        public String getTag() {
            return _actionTag;
        }

        public byte getCode() {
            return _actionCode;
        }
    }
}
