import socket
from typing import Set, Dict, List, Union

from mdde.registry.protocol import PRegistryWriteClient, PRegistryControlClient, PRegistryReadClient
from mdde.registry.container import RegistryResponse, BenchmarkStatus, BenchmarkResult
from mdde.registry.enums import ERegistryMode, EBenchmarkState
from .registry_response_json import RegistryResponseJson

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
        return RegistryResponseJson[bool](response)

    def ctrl_set_shuffle_mode(self) -> RegistryResponse[bool]:
        response = self._serialize_and_run_command('PREPSHUFFLE')
        return RegistryResponseJson[bool](response)

    def ctrl_populate_default_nodes(self) -> RegistryResponse[Set[str]]:
        response = self._serialize_and_run_command('INITNODES')
        return RegistryResponseJson[bool](response)

    def ctrl_generate_data(self, workload_id: str) -> RegistryResponse[bool]:
        response = self._serialize_and_run_command('BENCHLOAD', workload=workload_id)
        return RegistryResponseJson[bool](response)

    def ctrl_reset(self) -> RegistryResponse[bool]:
        response = self._serialize_and_run_command('RESET')
        return RegistryResponseJson[bool](response)

    def ctrl_get_mode(self) -> RegistryResponse[ERegistryMode]:
        response = self._serialize_and_run_command('REGMODE')
        try:
            response_state = ERegistryMode(response[RegistryResponseJson.R_RES])
        except ValueError:
            response_state = ERegistryMode.unknown

        return RegistryResponse[ERegistryMode](response_state,
                                               response[RegistryResponseJson.R_ERR],
                                               response[RegistryResponseJson.R_ERRCODE])

    def ctrl_sync_registry_to_data(self) -> RegistryResponse[bool]:
        response = self._serialize_and_run_command('RUNSHUFFLE')
        return RegistryResponseJson[bool](response)

    def ctrl_start_benchmark(self, workload_id: str) -> RegistryResponse[Dict]:
        response = self._serialize_and_run_command('RUNBENCH', workload=workload_id)
        return RegistryResponseJson[Dict](response)

    def ctrl_get_benchmark(self) -> RegistryResponse[BenchmarkStatus]:
        response = self._serialize_and_run_command('BENCHSTATE')
        benchmark_get_result: Union[None, BenchmarkStatus] = None

        result_dict: Dict = response[RegistryResponseJson.R_RES]
        if result_dict:
            stage: Union[None, EBenchmarkState] = None
            run_id: str = ''
            failed: bool = False
            completed: bool = False
            result: Union[None, BenchmarkResult] = None
            # Process basic properties of the benchmark response
            r_stage = result_dict.get('stage')
            if r_stage:
                try:
                    stage = EBenchmarkState(str(r_stage))
                except ValueError:
                    stage = None
            r_failed = result_dict.get('failed')
            if r_failed:
                failed = r_failed
            r_completed = result_dict.get('completed')
            if r_completed:
                completed = r_completed
            r_id = result_dict.get('id')
            if r_id:
                run_id = r_id
            # Process benchmark result
            r_result = result_dict.get('result')
            if r_result:
                throughput: float = -1.0
                error: Union[None, str] = None
                nodes: Union[None, List[Dict]] = None
                rr_error = r_result.get('error')
                if rr_error:
                    error = rr_error
                rr_throughput = r_result.get('throughput')
                if rr_throughput:
                    throughput = float(rr_throughput)
                rr_nodes = r_result.get('nodes')
                if rr_nodes != None:
                    nodes = rr_nodes
                result = BenchmarkResult(throughput, error, nodes)
            benchmark_get_result = BenchmarkStatus(stage, run_id, failed, completed, result)

        return RegistryResponse[BenchmarkStatus](benchmark_get_result,
                                                 response[RegistryResponseJson.R_ERR],
                                                 response[RegistryResponseJson.R_ERRCODE])

    def ctrl_flush(self) -> RegistryResponse[bool]:
        response = self._serialize_and_run_command('FLUSHALL')
        return RegistryResponseJson[bool](response)

    def ctrl_snapshot_create(self, as_default: bool) -> RegistryResponse[str]:
        response = self._serialize_and_run_command('SNAPSAVE', snapisdef=as_default)
        return RegistryResponseJson[str](response)

    def ctrl_snapshot_restore(self, snap_id: str) -> RegistryResponse[bool]:
        response = self._serialize_and_run_command('SNAPLOAD', snapid=snap_id)
        return RegistryResponseJson[bool](response)

    # READ commands

    def read_everything(self) -> RegistryResponse[str]:
        response = self._serialize_and_run_command('GETALL')
        return RegistryResponseJson[str](response)

    def read_get_all_fragments_with_meta(self,
                                         local_meta: Union[Set[str], None],
                                         global_meta: Union[Set[str], None]) -> RegistryResponse[Dict]:
        response = self._serialize_and_run_command('GETFRAGSWMETA', fmtagsloc=local_meta, fmtagsglb=global_meta)
        # TODO: Dedicated container class for deserialization
        response_catalog = response[RegistryResponseJson.R_RES]
        response_catalog["nodefrags"] = {int(k): v for k, v in response_catalog["nodefrags"].items()}
        exemplar_meta = response_catalog.get("fmexval", None)
        if exemplar_meta:
            response_catalog["fmexval"] = {int(k): v for k, v in exemplar_meta.items()}
        global_meta = response_catalog["fmglval"]
        if global_meta:
            response_catalog["fmglval"] = {int(k): v for k, v in global_meta.items()}
        return RegistryResponse[Dict](response_catalog,
                                      response[RegistryResponseJson.R_ERR],
                                      response[RegistryResponseJson.R_ERRCODE])

    def read_find_fragment(self, fragment_id: str) -> RegistryResponse[str]:
        response = self._serialize_and_run_command('FINDFRAGMENT', fid=fragment_id)
        return RegistryResponseJson[str](response)

    def read_count_fragment(self, fragment_id: str) -> RegistryResponse[int]:
        response = self._serialize_and_run_command('COUNTFRAGMENT', fid=fragment_id)
        return RegistryResponseJson[int](response)

    def read_nodes(self) -> RegistryResponse[Set[str]]:
        response = self._serialize_and_run_command('NODES')
        return RegistryResponseJson[str](response)

    def read_node_unassigned_tuples(self, node_id: str) -> RegistryResponse[Set[str]]:
        response = self._serialize_and_run_command('NTUPLESU', nid=node_id)
        return RegistryResponseJson[Set[str]](response)

    def read_node_fragments(self, node_id: str) -> RegistryResponse[Set[str]]:
        response = self._serialize_and_run_command('NFRAGS', nid=node_id)
        return RegistryResponseJson[Set[str]](response)

    def read_fragment_meta_on_exemplar(self, fragment_id: str, node_id: str, meta_tag: str) -> RegistryResponse[str]:
        response = self._serialize_and_run_command('GETMETAFRAGEXM', fid=fragment_id, nid=node_id, fmtag=meta_tag)
        return RegistryResponseJson[str](response)

    def read_fragment_meta_global(self, fragment_id: str, meta_tag: str) -> RegistryResponse[str]:
        response = self._serialize_and_run_command('GETMETAFRAGGLB', fid=fragment_id, fmtag=meta_tag)
        return RegistryResponseJson[str](response)

    # WRITE commands

    def write_fragment_create(self, fragment_id: str, tuple_ids: Set[str]) -> RegistryResponse[bool]:
        response = self._serialize_and_run_command('FRAGMAKE', fid=fragment_id, tids=tuple_ids)
        return RegistryResponseJson[bool](response)

    def write_fragment_append_tuple(self, fragment_id: str, tuple_id: str) -> RegistryResponse[bool]:
        response = self._serialize_and_run_command('FRAGAPPEND', fid=fragment_id, tid=tuple_id)
        return RegistryResponseJson[bool](response)

    def write_fragment_replicate(self, fragment_id: str, source_node_id: str, destination_node_id: str) \
            -> RegistryResponse[bool]:
        response = self._serialize_and_run_command('DCOPYFRAG',
                                                   fid=fragment_id, nid=source_node_id, nidb=destination_node_id)
        return RegistryResponseJson[bool](response)

    def write_fragment_delete_exemplar(self, fragment_id: str, node_id: str) -> RegistryResponse[bool]:
        response = self._serialize_and_run_command('DDELFRAGEX', fid=fragment_id, nid=node_id)
        return RegistryResponseJson[bool](response)

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
        return RegistryResponseJson[bool](response)

        pass

    def write_fragment_meta_global(self, fragment_id: str, meta_tag: str, meta_value: str) -> RegistryResponse[bool]:
        response = self._serialize_and_run_command('PUTMETAFRAGGLB',
                                                   fid=fragment_id,
                                                   fmtag=meta_tag,
                                                   fmval=meta_value)
        return RegistryResponseJson[bool](response)
