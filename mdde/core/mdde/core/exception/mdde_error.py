class MddeError(Exception):
    """Base class for the exceptions defined in MDDE"""
    def __init__(self, message=None):
        if type(self) is MddeError:
            raise Exception('MddeError cannot be instantiated directly. Create a concrete subclass for a '
                            'specific error.')
        super(MddeError, self).__init__(message)
