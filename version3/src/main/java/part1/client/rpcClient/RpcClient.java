package part1.client.rpcClient;


import part1.common.message.RpcRequest;
import part1.common.message.RpcResponse;

public interface RpcClient {
    RpcResponse sendRequest(RpcRequest rpcRequest);
}
