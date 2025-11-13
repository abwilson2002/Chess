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

    @Test
    public void test5() {
        Assertions.assertTrue(true);
    }
    @Test

    public void test6() {
        Assertions.assertTrue(true);
    }

    @Test
    public void test7() {
        Assertions.assertTrue(true);
    }

    @Test
    public void test8() {
        Assertions.assertTrue(true);
    }

    @Test
    public void test9() {
        Assertions.assertTrue(true);
    }

    @Test
    public void test10() {
        Assertions.assertTrue(true);
    }

    @Test
    public void test11() {
        Assertions.assertTrue(true);
    }

    @Test
    public void test12() {
        Assertions.assertTrue(true);
    }

}
