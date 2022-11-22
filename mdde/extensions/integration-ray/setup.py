from setuptools import setup, find_namespace_packages
import sys

CURRENT_PYTHON = sys.version_info[:2]
REQUIRED_PYTHON = (3, 7)

# Check the current python version
if CURRENT_PYTHON < REQUIRED_PYTHON:
    sys.stderr.write('MDDE requires Python {0[0]}.{0[1]} or higher. You have Python {1[0]}.{1[1]}.'
                     .format(REQUIRED_PYTHON, CURRENT_PYTHON))
    sys.exit(1)

setup(
    name='mdde.integration.ray',
    version='0.7',
    description='Multi-agent Data Distribution Environment: RAY RLlib integration',

    author='Andrey Kharitonov',
    author_email='andrey.kharitonov@ovgu.de',

    license='MIT Licence',
    packages=find_namespace_packages(include=['mdde.integration.ray.*'], exclude=['mdde.test.*']),

    install_requires=['gym==0.26.2',
                      'ray[rllib,tune]==2.1.0',
                      # 'tensorflow==1.15.2',
                      'tensorflow-probability==0.18.0',
                      'tabulate~=0.8.6',
                      'requests~=2.28.1',
                      'opencv-python~=4.6.0.66',
                      'psutil~=5.9.4',
                      'lz4~=2.2.1',
                      'setproctitle~=1.3.2',
                      'pandas>=1.5.1',
                      'dm-tree~=0.1.7'],
    zip_safe=False,
)
