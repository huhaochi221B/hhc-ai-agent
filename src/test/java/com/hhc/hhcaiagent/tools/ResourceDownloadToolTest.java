package com.hhc.hhcaiagent.tools;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
public class ResourceDownloadToolTest {

    @Test
    public void testDownloadResource() {
        ResourceDownloadTool tool = new ResourceDownloadTool();
//      String url = "https://www.codefather.cn/logo.png";
        String url = "https://mintcdn.com/clawdhub/-t5HSeZ3Y_0_wH4i/assets/openclaw-logo-text-dark.png?w=1650&fit=max&auto=format&n=-t5HSeZ3Y_0_wH4i&q=85&s=6eaae3d9d226837a0c97999308cc5774";
        String fileName = "logo.png";
        String result = tool.downloadResource(url, fileName);
        Assertions.assertNotNull(result);
    }
}