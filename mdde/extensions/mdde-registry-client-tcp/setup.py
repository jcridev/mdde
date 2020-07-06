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
    name='mdde.registry.tcp',
    version='0.7',
    description='Multi-agent Data Distribution Environment: TCP registry client',

    author='Andrey Kharitonov',
    author_email='andrey.kharitonov@ovgu.de',

    license='MIT Licence',
    packages=find_namespace_packages(include=['mdde.registry.*'], exclude=['mdde.test.*']),

    install_requires=['typing-extensions; python_version<"3.8"'],
    zip_safe=False,
)
