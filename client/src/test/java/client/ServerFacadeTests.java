package client;

import org.junit.jupiter.api.*;
import server.Server;


public class ServerFacadeTests {

    private static Server server;

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }


    @Test
    public void test1() {
        Assertions.assertTrue(true);
    }

    @Test
    public void test2() {
        Assertions.assertTrue(true);
    }

    @Test
    public void test3() {
        Assertions.assertTrue(true);
    }

    @Test
    public void test4() {
        Assertions.assertTrue(true);
    }

}
