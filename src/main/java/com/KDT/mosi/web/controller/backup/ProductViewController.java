//package com.KDT.mosi.web.controller.backup;
//
//import com.KDT.mosi.domain.entity.Member;
//import com.KDT.mosi.domain.member.svc.MemberSVC;
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.KDT.mosi.domain.entity.Product;
//import com.KDT.mosi.domain.entity.ProductCoursePoint;
//import com.KDT.mosi.domain.entity.ProductImage;
//import com.KDT.mosi.domain.product.svc.ProductCoursePointSVC;
//import com.KDT.mosi.domain.product.svc.ProductImageSVC;
//import com.KDT.mosi.domain.product.svc.ProductSVC;
//import com.KDT.mosi.web.form.product.ProductCoursePointForm;
//import com.KDT.mosi.web.form.product.ProductDetailForm;
//import com.KDT.mosi.web.form.product.ProductImageForm;
//import org.springframework.security.core.Authentication;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PathVariable;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.util.List;
//import java.util.stream.Collectors;
//
//@Controller
//public class ProductViewController {
//
//  private final ProductSVC productSVC;
//  private final ProductImageSVC productImageSVC;
//  private final ProductCoursePointSVC productCoursePointSVC;
//  private final MemberSVC memberSVC;
//
//  public ProductViewController(ProductSVC productSVC, ProductImageSVC productImageSVC, ProductCoursePointSVC productCoursePointSVC, MemberSVC memberSVC) {
//    this.productSVC = productSVC;
//    this.productImageSVC = productImageSVC;
//    this.productCoursePointSVC = productCoursePointSVC;
//    this.memberSVC = memberSVC;
//  }
//
//  // 타임리프 화면 뷰 반환 용
//  @GetMapping("/product/view/{id}")
//  public String productDetailView(@PathVariable Long id, Model model,
//                                  @RequestParam(value="file", required = false)MultipartFile file,
//                                  Authentication authentication) {
//    String email = authentication.getName();
//    Member member = memberSVC.findByEmail(email).orElseThrow();
//    Product product = productSVC.getProductById(id);
//    if (product == null) {
//      return "error/404"; // 404.html
//    }
//    List<ProductImage> imageList = productImageSVC.getImagesByProductId(id);
//    List<ProductCoursePoint> pointList = productCoursePointSVC.getPointsByProductId(id);
//
//    Long memberId = product.getMemberid();
//
//    ProductDetailForm detail = new ProductDetailForm();
//    detail.setProductId(product.getProductId());
//    detail.setMemberId(memberId);
//    detail.setCategory(product.getCategory());
//    detail.setTitle(product.getTitle());
//    detail.setGuideYn(product.getGuideYn());
//    detail.setNormalPrice(product.getNormalPrice());
//    detail.setGuidePrice(product.getGuidePrice());
//    detail.setSalesPrice(product.getSalesPrice());
//    detail.setSalesGuidePrice(product.getSalesGuidePrice());
//    detail.setTotalDay(product.getTotalDay());
//    detail.setTotalTime(product.getTotalTime());
//    detail.setReqMoney(product.getReqMoney());
//    detail.setSleepInfo(product.getSleepInfo());
//    detail.setTransportInfo(product.getTransportInfo());
//    detail.setFoodInfo(product.getFoodInfo());
//    detail.setReqPeople(product.getReqPeople());
//    detail.setTarget(product.getTarget());
//    detail.setStucks(product.getStucks());
//    detail.setDescription(product.getDescription());
//    detail.setDetail(product.getDetail());
//    detail.setFileName(product.getFileName());
//    detail.setFileType(product.getFileType());
//    detail.setFileSize(product.getFileSize());
//    detail.setFileData(product.getFileData());
//    detail.setPriceDetail(product.getPriceDetail());
//    detail.setGpriceDetail(product.getGpriceDetail());
//    detail.setStatus(product.getStatus());
//    detail.setCreateDate(product.getCreateDate());
//    detail.setUpdateDate(product.getUpdateDate());
//
//    List<ProductImageForm> images = imageList.stream().map(pi -> {
//      ProductImageForm imgForm = new ProductImageForm();
//      imgForm.setImageId(pi.getImageId());
//      imgForm.setProductId(pi.getProductId());
//      if (pi.getImageData() != null) {
//        String base64 = java.util.Base64.getEncoder().encodeToString(pi.getImageData());
//        imgForm.setBase64ImageData("data:" + pi.getMimeType() + ";base64," + base64);
//      }
//      imgForm.setImageOrder(pi.getImageOrder());
//      imgForm.setFileName(pi.getFileName());
//      imgForm.setFileSize(pi.getFileSize());
//      imgForm.setMimeType(pi.getMimeType());
//      imgForm.setUploadTime(pi.getUploadTime());
//      return imgForm;
//    }).collect(Collectors.toList());
//    detail.setProductImages(images);
//
//    List<ProductCoursePointForm> points = pointList.stream().map(pcp -> {
//      ProductCoursePointForm pcpForm = new ProductCoursePointForm();
//      pcpForm.setCoursePointId(pcp.getCoursePointId());
//      pcpForm.setPointOrder(pcp.getPointOrder());
//      pcpForm.setLatitude(pcp.getLatitude());
//      pcpForm.setLongitude(pcp.getLongitude());
//      pcpForm.setDescription(pcp.getDescription());
//      return pcpForm;
//    }).collect(Collectors.toList());
//    detail.setCoursePoints(points);
//
//
//    model.addAttribute("product", detail);
//
//
//    ObjectMapper mapper = new ObjectMapper(); // 선언 후 사용
//    String jsonStr = null;
//    try {
//      jsonStr = mapper.writeValueAsString(product);
//    } catch (JsonProcessingException e) {
//      throw new RuntimeException(e);
//    }
//    System.out.println("product: " + product);
//    System.out.println("product JSON: " + jsonStr);
//
//    model.addAttribute("productJson", jsonStr);
//
//    return "product/product_detail";   // => src/main/resources/templates/product/detail.html 렌더링
//  }
//}