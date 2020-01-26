import json


class Serializer:
    """Serialize and deserialize JSON responses from the TCP registry api"""

    @staticmethod
    def serialize_command(command_tag: str, **kwargs) -> str:
        command_args = {}
        command_json = {"cmd": command_tag, "args": command_args}

        for key, value in kwargs.items():
            if isinstance(value, set):
                command_args[key] = list(value)
            else:
                command_args[key] = value

        return json.dumps(command_json)

    @staticmethod
    def deserialize_response(json_string: str) -> {}:
        return json.loads(json_string)

