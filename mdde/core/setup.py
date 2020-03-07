from setuptools import setup, find_namespace_packages
import sys

CURRENT_PYTHON = sys.version_info[:2]
REQUIRED_PYTHON = (3, 7)

# Check the current python version
if CURRENT_PYTHON < REQUIRED_PYTHON:
    sys.stderr.write('MDDE requires Python {0[0]}.{0[1]} or higher. You have Python {1[0]}.{1[1]}.'
                     .format(REQUIRED_PYTHON, CURRENT_PYTHON))
    sys.exit(1)


packages = find_namespace_packages(include=['mdde.*'], exclude=['mdde.test.*'])

requirements = ['typing-extensions; python_version<"3.8"',
                'tiledb>=0.5.6',  # conda install -c conda-forge tiledb-py
                'PyYAML']

setup(
    name='mdde',
    version='0.1',
    description='Multi-agent Data Distribution Environment',

    author='Andrey Kharitonov',
    author_email='andrey.kharitonov@ovgu.de',

    license='MIT Licence',
    packages=packages,


    install_requires=requirements,
    zip_safe=False,
)
