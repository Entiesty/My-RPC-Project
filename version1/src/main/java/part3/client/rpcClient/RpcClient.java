package part3.client.rpcClient;

import part3.common.message.RpcRequest;
import part3.common.message.RpcResponse;

public interface RpcClient {
    RpcResponse sendRequest(RpcRequest rpcRequest);
}
