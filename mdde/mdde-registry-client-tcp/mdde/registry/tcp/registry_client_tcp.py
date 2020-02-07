import socket
from typing import Set, Dict

from mdde.registry.protocol import PRegistryWriteClient, PRegistryControlClient, PRegistryReadClient
from mdde.registry import RegistryResponse

from .serializer import Serializer


class RegistryClientTCP(PRegistryWriteClient, PRegistryReadClient, PRegistryControlClient):
    """Registry client utilizing TCP Socket connection for communication with the registry"""

    _default_encoding = "utf-8"

    def __init__(self, host: str, port: int):
        """
        Constructor
        :param host: Host (domain, ip, localhost, etc.) where the MDDE registry is running
        :param port: Port where the MDDE registry control socket is listening
        """
        self._registry_server = (host, port)

    def _execute_tcp_call(self, message: str) -> str:
        """
        Connect to the registry, send the JSON serialized command, retrieve JSON serialized response
        :param message: JSON request
                        Example: {"cmd":"PREPBENCHMARK", "args": null}
        :return: JSON string
        """
        msg_marshalled = self._marshall_message(message)
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
                received_length += len(len_bytes)
                length_buf.extend(len_bytes)
            # Decode expected length of the response
            expected_response_length = self._bytes_to_number(length_buf)
            # Read response JSON payload
            response_buf = bytearray(0)
            received_length = 0
            while received_length < expected_response_length:
                payload_bytes_chunk = reg_socket.recv(expected_response_length)
                if not payload_bytes_chunk:
                    raise EOFError("No response payload from the server received")
                received_length += len(payload_bytes_chunk)
                response_buf.extend(payload_bytes_chunk)
            # Decode retrieved message
            return self._decode_message(response_buf)
        finally:
            reg_socket.close()

    def _marshall_message(self, message: str) -> []:
        """
        Encode the JSON payload to byte array and marshall by attaching leading length header
        :param message: JSON serialized into a string
        :return: JSON marshalled to a byte array ready from transmission
        """
        msg_bytes = bytes(message, self._default_encoding)
        msg_len = len(msg_bytes)
        return self._number_to_bytes(msg_len) + msg_bytes

    def _decode_message(self, message_bytes: []) -> str:
        """
        Decode byte array of the unmarshalled byte array containing a JSON serialized into a string
        :param message_bytes: Unmarshalled byte array
        :return: String containing the response JSON
        """
        return message_bytes.decode(self._default_encoding)

    @staticmethod
    def _number_to_bytes(number: int) -> []:
        """Encode numeric value into 4 bytes (big endian)"""
        return number.to_bytes(4, byteorder='big')

    @staticmethod
    def _bytes_to_number(number_bytes: []) -> int:
        """Decode numeric value from bytes (big endian)"""
        return int.from_bytes(number_bytes, byteorder='big')

    def _serialize_and_run_command(self, cmd: str, **kwargs) -> {}:
        """
        Call a command that requires no arguments
        :param cmd: Command tag
        :param kwargs Command arguments [key=value,...]
        :return: Object - deserialized response JSON
        """
        serialized_command = Serializer.serialize_command(cmd, **kwargs)
        serialized_response = self._execute_tcp_call(serialized_command)
        return Serializer.deserialize_response(serialized_response)

    # CONTROL commands

    def ctrl_set_benchmark_mode(self) -> RegistryResponse[bool]:
        response = self._serialize_and_run_command('PREPBENCHMARK')
        return RegistryResponse[bool](response['result'], response['error'])

    def ctrl_set_shuffle_mode(self) -> RegistryResponse[bool]:
        response = self._serialize_and_run_command('PREPSHUFFLE')
        return RegistryResponse[bool](response['result'], response['error'])

    def ctrl_generate_data(self) -> RegistryResponse[bool]:
        response = self._serialize_and_run_command('BENCHLOAD')
        return RegistryResponse[bool](response['result'], response['error'])

    def ctrl_reset(self) -> RegistryResponse[bool]:
        response = self._serialize_and_run_command('RESET')
        return RegistryResponse[bool](response['result'], response['error'])

    def ctrl_get_mode(self) -> RegistryResponse[Dict]:
        response = self._serialize_and_run_command('BENCHSTATE')
        return RegistryResponse[Dict](response['result'], response['error'])

    def ctrl_sync_registry_to_data(self) -> RegistryResponse[bool]:
        response = self._serialize_and_run_command('RUNSHUFFLE')
        return RegistryResponse[bool](response['result'], response['error'])

    def ctrl_start_benchmark(self, workload_id: str) -> RegistryResponse[Dict]:
        response = self._serialize_and_run_command('RUNSHUFFLE', workload=workload_id)
        return RegistryResponse[Dict](response['result'], response['error'])

    def ctrl_get_benchmark(self) -> RegistryResponse[Dict]:
        response = self._serialize_and_run_command('BENCHRUN')
        return RegistryResponse[Dict](response['result'], response['error'])

    def ctrl_flush(self) -> RegistryResponse[bool]:
        response = self._serialize_and_run_command('FLUSHALL')
        return RegistryResponse[bool](response['result'], response['error'])

    def ctrl_snapshot_create(self, as_default: bool) -> RegistryResponse[str]:
        response = self._serialize_and_run_command('SNAPSAVE', snapisdef=as_default)
        return RegistryResponse[str](response['result'], response['error'])

    def ctrl_snapshot_restore(self, snap_id: str) -> RegistryResponse[bool]:
        response = self._serialize_and_run_command('SNAPLOAD', snapid=snap_id)
        return RegistryResponse[bool](response['result'], response['error'])

    # READ commands

    def read_everything(self) -> RegistryResponse[str]:
        response = self._serialize_and_run_command('GETALL')
        return RegistryResponse[str](response['result'], response['error'])

    def read_get_all_fragments_with_meta(self, local_meta: Set[str], global_meta: Set[str]) -> RegistryResponse[Dict]:
        response = self._serialize_and_run_command('GETFRAGSWMETA', fmtagsloc=local_meta, fmtagsglb=global_meta)
        return RegistryResponse[Dict](response['result'], response['error'])

    def read_find_fragment(self, fragment_id: str) -> RegistryResponse[str]:
        response = self._serialize_and_run_command('FINDFRAGMENT', fid=fragment_id)
        return RegistryResponse[str](response['result'], response['error'])

    def read_count_fragment(self, fragment_id: str) -> RegistryResponse[int]:
        response = self._serialize_and_run_command('COUNTFRAGMENT', fid=fragment_id)
        return RegistryResponse[int](response['result'], response['error'])

    def read_nodes(self) -> RegistryResponse[Set[str]]:
        response = self._serialize_and_run_command('NODES')
        return RegistryResponse[str](response['result'], response['error'])

    def read_node_unassigned_tuples(self, node_id: str) -> RegistryResponse[Set[str]]:
        response = self._serialize_and_run_command('NTUPLESU', nid=node_id)
        return RegistryResponse[Set[str]](response['result'], response['error'])

    def read_node_fragments(self, node_id: str) -> RegistryResponse[Set[str]]:
        response = self._serialize_and_run_command('NFRAGS', nid=node_id)
        return RegistryResponse[Set[str]](response['result'], response['error'])

    def read_fragment_meta_on_exemplar(self, fragment_id: str, node_id: str, meta_tag: str) -> RegistryResponse[str]:
        response = self._serialize_and_run_command('GETMETAFRAGEXM', fid=fragment_id, nid=node_id, fmtag=meta_tag)
        return RegistryResponse[str](response['result'], response['error'])

    def read_fragment_meta_global(self, fragment_id: str, meta_tag: str) -> RegistryResponse[str]:
        response = self._serialize_and_run_command('GETMETAFRAGGLB', fid=fragment_id, fmtag=meta_tag)
        return RegistryResponse[str](response['result'], response['error'])

    # WRITE commands

    def write_fragment_create(self, fragment_id: str, tuple_ids: Set[str]) -> RegistryResponse[str]:
        response = self._serialize_and_run_command('FRAGMAKE', fid=fragment_id, tids=tuple_ids)
        return RegistryResponse[str](response['result'], response['error'])

    def write_fragment_append_tuple(self, fragment_id: str, tuple_id: str) -> RegistryResponse[bool]:
        response = self._serialize_and_run_command('FRAGAPPEND', fid=fragment_id, tid=tuple_id)
        return RegistryResponse[bool](response['result'], response['error'])

    def write_fragment_replicate(self, fragment_id: str, source_node_id: str, destination_node_id: str):
        response = self._serialize_and_run_command('DCOPYFRAG',
                                                   fid=fragment_id, nid=source_node_id, nidb=destination_node_id)
        return RegistryResponse[str](response['result'], response['error'])

    def write_fragment_delete_exemplar(self, fragment_id: str, node_id: str) -> RegistryResponse[str]:
        response = self._serialize_and_run_command('DDELFRAGEX', fid=fragment_id, nid=node_id)
        return RegistryResponse[str](response['result'], response['error'])

    def write_fragment_meta_on_copy(self,
                                    fragment_id: str,
                                    node_id: str,
                                    meta_tag: str,
                                    meta_value: str) -> RegistryResponse[bool]:

        response = self._serialize_and_run_command('PUTMETAFRAGEXM',
                                                   fid=fragment_id,
                                                   nid=node_id,
                                                   fmtag=meta_tag,
                                                   fmval=meta_value)
        return RegistryResponse[bool](response['result'], response['error'])

        pass

    def write_fragment_meta_global(self, fragment_id: str, meta_tag: str, meta_value: str) -> RegistryResponse[bool]:
        response = self._serialize_and_run_command('PUTMETAFRAGGLB',
                                                   fid=fragment_id,
                                                   fmtag=meta_tag,
                                                   fmval=meta_value)
        return RegistryResponse[bool](response['result'], response['error'])
