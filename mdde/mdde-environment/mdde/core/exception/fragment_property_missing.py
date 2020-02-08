from mdde.core.exception import MddeError


class FragmentInstanceMissingPropertyError(MddeError):
    """Error creating an instance of the environment"""

    def __init__(self, fragment_id: str):
        super(FragmentInstanceMissingPropertyError, self).__init__("Current object for fragment {} was retrieved "
                                                                   "without meta values".format(fragment_id))
