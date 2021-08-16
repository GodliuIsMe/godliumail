package com.liu.mallproduct;

import com.liu.mallproduct.entity.BrandEntity;
import com.liu.mallproduct.service.BrandService;
import com.liu.mallproduct.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;

@Slf4j
@SpringBootTest
class MallProductApplicationTests {
    @Autowired
    BrandService brandService;

//    @Autowired
//    OSSClient ossClient2;

    @Autowired
    CategoryService categoryService;

    @Test
    public void test3(){
        Long[] catelogPath = categoryService.findCateLogPath(226L);
        log.info("完整路径:{}", Arrays.asList(catelogPath));
    }

//    @Test
//    public void test2() throws FileNotFoundException{
//        InputStream is = new FileInputStream("C:\\Users\\godliu\\Pictures\\舒畅\\1.jpeg");
//        ossClient2.putObject("mall-liu","2.jpeg",is);
//        ossClient2.shutdown();
//        System.out.println("上传完成");
//
//    }

//    @Test
//    public void testUpload() throws FileNotFoundException {
//        // yourEndpoint填写Bucket所在地域对应的Endpoint。以华东1（杭州）为例，Endpoint填写为https://oss-cn-hangzhou.aliyuncs.com。
//        String endpoint = "oss-cn-shanghai.aliyuncs.com";
//        // 阿里云账号AccessKey拥有所有API的访问权限，风险很高。强烈建议您创建并使用RAM用户进行API访问或日常运维，请登录RAM控制台创建RAM用户。
//        String accessKeyId = "LTAI5tLtRXU4L21VKEfAjghz";
//        String accessKeySecret = "LtSWKAnGoWAZqYcSqdXejWhlg29VOU";
//
//        // 创建OSSClient实例。
//        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
//
//        // 上传文件流
//        InputStream is = new FileInputStream("C:\\Users\\godliu\\Pictures\\舒畅\\1.jpeg");
//        ossClient.putObject("mall-liu","1.jpeg",is);
//
//        // 创建PutObjectRequest对象。
//        // 依次填写Bucket名称（例如examplebucket）和Object完整路径（例如exampledir/exampleobject.txt）。Object完整路径中不能包含Bucket名称。
////        PutObjectRequest putObjectRequest = new PutObjectRequest("examplebucket", "exampledir/exampleobject.txt", new ByteArrayInputStream(content.getBytes()));
//
//        // 如果需要上传时设置存储类型和访问权限，请参考以下示例代码。
//        // ObjectMetadata metadata = new ObjectMetadata();
//        // metadata.setHeader(OSSHeaders.OSS_STORAGE_CLASS, StorageClass.Standard.toString());
//        // metadata.setObjectAcl(CannedAccessControlList.Private);
//        // putObjectRequest.setMetadata(metadata);
//
//        // 上传字符串。
////        ossClient.putObject(putObjectRequest);
//
//        // 关闭OSSClient。
//        ossClient.shutdown();
//        System.out.println("上传成功");
//    }

    @Test
    void contextLoads() {
        BrandEntity brandEntity = new BrandEntity();
        brandEntity.setName("aaaa");
        brandService.save(brandEntity);
        System.out.println("保存成功");
    }

}
