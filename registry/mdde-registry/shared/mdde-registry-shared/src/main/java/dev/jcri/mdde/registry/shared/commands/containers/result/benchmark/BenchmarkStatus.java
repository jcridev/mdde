package dev.jcri.mdde.registry.shared.commands.containers.result.benchmark;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;

/**
 * Container class for the benchmark status and results
 */
public class BenchmarkStatus {
    public static final String COMPLETED_FIELD = "completed";
    public static final String FAILED_FIELD = "failed";
    public static final String STAGE_FIELD = "stage";
    public static final String RESULT_FIELD = "result";
    public static final String RUN_ID_FIELD = "id";

    private boolean _isCompleted = false;
    private boolean _isFailed = false;
    private String _currentStage = null;
    private BenchmarkRunResult _result = null;
    private String _runId = null;

    /**
     * Default constructor
     */
    public BenchmarkStatus(){}

    /**
     * Constructor
     * @param runCompleted True - the run was executed and completed
     * @param runFailed True - the run was executed but failed
     * @param runStage Current execution stage
     * @param runResult If the run was executed there the result will be generated, even if it failed.
     *                  For a failed run, error will be supplied instead of the results
     * @param runId Executed run ID
     */
    public BenchmarkStatus(boolean runCompleted,
                           boolean runFailed,
                           String runStage,
                           BenchmarkRunResult runResult,
                           String runId){
        _runId = runId;
        _isCompleted = runCompleted;
        _isFailed = runFailed;
        _currentStage = runStage;
        _result = runResult;
    }

    /**
     * Completion status of the benchmark
     * @return False - benchmark was not yet run or still running
     */
    @JsonGetter(COMPLETED_FIELD)
    public boolean isCompleted() {
        return _isCompleted;
    }
    @JsonSetter(COMPLETED_FIELD)
    public void setCompleted(boolean completed) {
        _isCompleted = completed;
    }

    /**
     * Error status
     * @return True - there was an error during the benchmark run execution
     */
    @JsonGetter(FAILED_FIELD)
    public boolean isFailed() {
        return _isFailed;
    }
    @JsonSetter(FAILED_FIELD)
    public void setFailed(boolean failed) {
        _isFailed = failed;
    }

    /**
     * Current execution stage
     * @return String tag for the current state of the benchmark
     */
    @JsonGetter(STAGE_FIELD)
    public String getCurrentStage() {
        return _currentStage;
    }
    @JsonSetter(STAGE_FIELD)
    public void setCurrentStage(String currentStage) {
        this._currentStage = currentStage;
    }

    /**
     * If the benchmark was completed successfully, the field contains values returned by the benchmark
     * @return Throughput, request frequencies, etc.
     */
    @JsonGetter(RESULT_FIELD)
    public BenchmarkRunResult getResult() {
        return _result;
    }
    @JsonSetter(RESULT_FIELD)
    public void setResult(BenchmarkRunResult result) {
        this._result = result;
    }

    /**
     * Unique identifier for the benchmark run. Not sequential values, simple UUIDs to distinguish one invocation of the
     * benchmark run from another on the client side, when and if it's needed.
     * @return String UUID
     */
    @JsonGetter(RUN_ID_FIELD)
    public String getRunId() {
        return _runId;
    }
    @JsonSetter(RUN_ID_FIELD)
    public void setRunId(String runId) {
        this._runId = runId;
    }
}
