package part2.client.rpcClient;


import part2.common.message.RpcRequest;
import part2.common.message.RpcResponse;

public interface RpcClient {
    RpcResponse sendRequest(RpcRequest rpcRequest);
}
