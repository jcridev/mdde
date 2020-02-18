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

with open("README.md", "r") as fh:
    long_description = fh.read()

setup(
    name='mdde_stats',
    version='0.1',
    description='Benchmark statistics processor for Multi-agent Data Distribution Environment',
    long_description=long_description,
    long_description_content_type="text/markdown",
    url="https://github.com/jcridev/mdde",

    keywords="mdde data-distribution reinforcement-learning deep-learning python",
    classifiers=[
            "Programming Language :: Python :: 3",
            "License :: OSI Approved :: MIT License",
            "Operating System :: OS Independent",
        ],
    python_requires='>=3.7',

    author='Andrey Kharitonov',
    author_email='andrey.kharitonov@ovgu.de',

    license='MIT Licence',
    packages=['mdde_stats'],
    package_dir={'': 'src'},
    #packages=find_packages(where='./src'),

    install_requires=requires,
    zip_safe=False,
)
