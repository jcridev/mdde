from setuptools import setup, find_packages
import sys

CURRENT_PYTHON = sys.version_info[:2]
REQUIRED_PYTHON = (3, 7)

# Check the current python version
if CURRENT_PYTHON < REQUIRED_PYTHON:
    sys.stderr.write('MDDE Stats processor requires Python {0[0]}.{0[1]} or higher. You have Python {1[0]}.{1[1]}.'
                     .format(REQUIRED_PYTHON, CURRENT_PYTHON))
    sys.exit(1)

requires = [
    "kafka-python==2.0.0"
]

setup(
    name='MDDE Stats',
    version='0.1',
    description='Benchmark statistics processor for Multi-agent Data Distribution Environment',
    url="https://github.com/jcridev/mdde",
    keywords="mdde data-distribution reinforcement-learning deep-learning python",

    author='Andrey Kharitonov',
    author_email='andrey.kharitonov@ovgu.de',

    license='MIT Licence',
    packages=find_packages(where='./src'),

    install_requires=requires,
    zip_safe=False,
)
