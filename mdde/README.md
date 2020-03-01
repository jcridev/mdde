<!-- omit in toc -->
# Multi-agent Data Distribution Environment

This is the code that presents MDDE environment to the learners. It is the layer between a reinforcement learning framework and the data distribution infrastructure.

This is the place where behavior of the agents, scenarios and the reward functions are defined.

- [Installation](#installation)
  - [Environment](#environment)
  - [MDDE Core](#mdde-core)
  - [Extensions](#extensions)
- [Configuration](#configuration)
- [Extending the code](#extending-the-code)

## Installation 

You can install the packages locally from the GIT checkout.

### Environment

If you're using Conda environments, check `./support` folder for an appropriate base environment configuration.

### MDDE Core

Core packages of the environment. Must always be present for MDDE to work.

```
pip install -e ./core
```

### Extensions

Registry client:
* TCP based: `pip install -e ./extensions/mdde-registry-client-tcp`


Learners integration:
* Ray RLlib: `pip install -e ./extensions/integration-ray`


## Configuration

...

## Extending the code

You can add new agents, scenarios, custom fragmentation logic, and registry-client implementations and learner integrations without directly editing MDDE Core code.
We utilize [implicit namespace packages](https://packaging.python.org/guides/packaging-namespace-packages/) \([PEP 420](https://www.python.org/dev/peps/pep-0420/)\) for the definition of extensions.

Following namespaces are available for extension:

* mdde.scenario
* mdde.integration
* mdde.fragmentation
* mdde.agent