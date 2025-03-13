package part3.client.rpcClient.impl;

import lombok.RequiredArgsConstructor;
import part3.client.rpcClient.RpcClient;
import part3.common.message.RpcRequest;
import part3.common.message.RpcResponse;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

@RequiredArgsConstructor
public class SimpleSocketRpcClient implements RpcClient {
    private final String host;
    private final int port;

    @Override
    public RpcResponse sendRequest(RpcRequest rpcRequest) {
        try (
                Socket socket = new Socket(host, port);
                ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
                ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
        ) {
            outputStream.writeObject(rpcRequest);
            outputStream.flush();

            return (RpcResponse) inputStream.readObject();

        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
