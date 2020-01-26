import socket

from mdde.registry.PRegistryClient import PRegistryClient
from mdde.registry.tcp.Serializer import Serializer


class RegistryClientTCP(PRegistryClient):
    """Registry client utilizing TCP Socket connection for communication with the registry"""

    __default_encoding__ = "utf-8"

    def __init__(self, host: str, port: int):
        """
        Constructor
        :param host: Host (domain, ip, localhost, etc.) where the MDDE registry is running
        :param port: Port where the MDDE registry control socket is listening
        """
        self._registry_server = (host, port)

    def __execute_tcp_call__(self, message: str) -> str:
        """
        Connect to the registry, send the JSON serialized command, retrieve JSON serialized response
        :param message: JSON request
                        Example: {"cmd":"PREPBENCHMARK", "args": null}
        :return: JSON string
        """
        msg_marshalled = self.__marshall_message__(message)
        reg_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        reg_socket.connect(self._registry_server)
        try:
            reg_socket.sendall(msg_marshalled)
            # Read the response length
            len_header_len = 4
            length_buf = bytearray(0)
            received_length = 0
            while received_length < len_header_len:
                len_bytes = reg_socket.recv(4)
                if not len_bytes:
                    raise EOFError("No response from the server received")
                length_buf += len_bytes
            # Decode expected length of the response
            reg_socket.recv_into(length_buf, 4)
            expected_response_length = self.__bytes_to_number__(length_buf)
            # Read response JSON payload
            response_buf = bytearray(0)
            received_length = 0
            while received_length < expected_response_length:
                payload_bytes_chunk = reg_socket.recv(expected_response_length)
                received_length += len(payload_bytes_chunk)
                response_buf += payload_bytes_chunk
            # Decode retrieved message
            return self.__decode_message__(response_buf)
        finally:
            reg_socket.close()

    def __marshall_message__(self, message: str) -> []:
        """Encode the JSON payload to byte array"""
        msg_bytes = bytes(message, self.__default_encoding__)
        msg_len = len(msg_bytes)
        return self.__number_to_bytes__(msg_len) + msg_bytes

    def __decode_message__(self, message_bytes: []) -> str:
        return message_bytes.decode(self.__default_encoding__)

    @staticmethod
    def __number_to_bytes__(number: int) -> []:
        """Encode numeric value into 4 bytes (big endian)"""
        return number.to_bytes(4, byteorder='big')

    @staticmethod
    def __bytes_to_number__(number_bytes: []) -> int:
        """Decode numeric value from bytes (big endian)"""
        return int.from_bytes(number_bytes, byteorder='big')

    def ctrl_set_benchmark_mode(self) -> bool:
        cmd = 'PREPBENCHMARK'
        serialized_command = Serializer.serialize_command(cmd)
        serialized_response = self.__execute_tcp_call__(serialized_command)
        response = Serializer.deserialize_response(serialized_response)
        return response['result']

    def ctrl_set_shuffle_mode(self) -> bool:
        pass

    def ctrl_generate_data(self) -> bool:
        pass

    def ctrl_run_benchmark(self) -> str:
        pass

    def ctrl_reset(self) -> bool:
        pass

    def ctrl_get_mode(self) -> str:
        pass

    def ctrl_sync_registry_to_data(self) -> str:
        pass
