class FragmentInstanceMissingMetaError(Exception):
    """Error creating an instance of the environment"""

    def __init__(self, fragment_id: str):
        super(FragmentInstanceMissingMetaError, self).__init__("Current object for fragment {} "
                                                               "was retrieved without meta values".format(fragment_id))
