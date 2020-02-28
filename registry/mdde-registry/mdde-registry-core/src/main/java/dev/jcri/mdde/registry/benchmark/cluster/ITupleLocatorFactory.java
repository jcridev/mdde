package dev.jcri.mdde.registry.benchmark.cluster;

/**
 * Factory of instances for tuple locators
 */
public interface ITupleLocatorFactory {
    IReadOnlyTupleLocator getNewTupleLocator();
}
