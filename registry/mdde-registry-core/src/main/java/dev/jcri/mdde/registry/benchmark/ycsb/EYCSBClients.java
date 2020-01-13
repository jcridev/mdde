package dev.jcri.mdde.registry.benchmark.ycsb;

public enum EYCSBClients {
    MDDE_REDIS("mdde.redis");

    private final String _clientName;

    EYCSBClients(String clientName){
        if(clientName == null || clientName.isBlank()){
            throw new IllegalArgumentException("YCSB Client name can't be null or empty");
        }

        _clientName = clientName;
    }

    public String getClientName(){
        return _clientName;
    }


    @Override
    public String toString() {
        return _clientName;
    }
}
