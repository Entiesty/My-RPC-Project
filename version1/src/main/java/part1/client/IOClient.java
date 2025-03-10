package part1.client;

import part1.common.message.RpcRequest;
import part1.common.message.RpcResponse;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class IOClient {
    /**
     * 发送 RPC 请求到指定的服务器，并接收响应。
     *
     * @param host    服务器的主机地址（例如 "localhost" 或 "192.168.1.1"）。
     * @param port    服务器的端口号（例如 8080）。
     * @param request 要发送的 RPC 请求对象。
     * @return 服务器返回的 RPC 响应对象；如果发生错误，则返回 null。
     */
    public static RpcResponse sendRequest(String host, int port, RpcRequest request) {
        // 使用 try-with-resources 语句自动管理资源（Socket、ObjectOutputStream、ObjectInputStream）
        try (Socket socket = new Socket(host, port); // 创建与服务器的 Socket 连接
             ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream()); // 创建输出流，用于发送请求
             ObjectInputStream ois = new ObjectInputStream(socket.getInputStream())) { // 创建输入流，用于接收响应

            // 将 RPC 请求对象序列化并发送到服务器
            oos.writeObject(request);
            oos.flush(); // 刷新输出流，确保数据立即发送

            // 从服务器读取响应并反序列化为 RpcResponse 对象
            return (RpcResponse) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            // 捕获可能的异常：
            // - IOException：网络通信或流操作失败（例如连接失败、流关闭等）。
            // - ClassNotFoundException：反序列化时找不到类定义。
            System.out.println("Error occurred: " + e.getMessage()); // 打印错误信息
            return null; // 返回 null 表示请求失败
        }
    }
}
