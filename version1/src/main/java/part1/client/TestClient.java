package part1.client;

import part1.client.proxy.ClientProxy;
import part1.common.pojo.User;
import part1.common.service.UserService;

public class TestClient {
    public static void main(String[] args) {
        ClientProxy clientProxy = new ClientProxy("127.0.0.1", 9999);
        UserService proxy = clientProxy.getProxy(UserService.class);

        User user = proxy.getUserByUserId(1);
        System.out.println("从服务端获得的用户：" + user.toString());

        User u = User.builder()
                .id(1)
                .userName("Boogiepop")
                .sex(true)
                .build();
        Integer id = proxy.insertUserId(u);
        System.out.println("向服务端插入的id：" + id);
    }
}
