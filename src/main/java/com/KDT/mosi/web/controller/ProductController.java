package com.KDT.mosi.web.controller;

import com.KDT.mosi.domain.entity.*;
import com.KDT.mosi.domain.mypage.seller.svc.SellerPageSVC;
import com.KDT.mosi.domain.product.svc.ProductCoursePointSVC;
import com.KDT.mosi.domain.product.svc.ProductImageSVC;
import com.KDT.mosi.domain.product.svc.ProductSVC;
import com.KDT.mosi.web.form.product.ProductCoursePointForm;
import com.KDT.mosi.web.form.product.ProductUploadForm;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Controller
@RequestMapping("/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductSVC productSVC;
    private final ProductImageSVC productImageSVC;
    private final ProductCoursePointSVC productCoursePointSVC;
    private final SellerPageSVC sellerPageSVC;

    @GetMapping("/list")
    public String list(Model model,
                       @RequestParam(name = "page", defaultValue = "1") int page,
                       @RequestParam(name = "size", defaultValue = "12") int size) {
        List<Product> products = productSVC.getProductsByPage(page, size);
        model.addAttribute("products", products);
        model.addAttribute("currentPage", page);
        return "product/product_list";
    }

    @GetMapping("/upload")
    public String uploadForm(Model model, HttpSession session, RedirectAttributes redirectAttrs) {

        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) {
            redirectAttrs.addFlashAttribute("redirectAfterLogin", "/product/upload");
            return "redirect:/login";
        }
        model.addAttribute("nickname", loginMember.getNickname());
        model.addAttribute("productUploadForm", new ProductUploadForm());
        return "product/enroll(without_map)";
    }

    @PostMapping("/upload")
    public String uploadSubmit(@ModelAttribute ProductUploadForm form, HttpSession session ,Model model) throws IOException {

        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) {
            return "redirect:/login";
        }

        // 로그인한 회원 ID를 form에 세팅
        form.setMemberId(loginMember.getMemberId());

        if (form.getProductImages() != null && form.getProductImages().size() > 10) {
            model.addAttribute("errorMessage", "이미지는 최대 10장까지 업로드 가능합니다.");
            return "product/upload";
        }

        Product product = toEntity(form);
        product.setCreateDate(new Date(System.currentTimeMillis()));

        Product savedProduct = productSVC.registerProduct(product);
        if (savedProduct == null || savedProduct.getProductId() == null) {
            throw new IllegalStateException("상품 등록 후 productId가 없습니다.");
        }

        // 다중 이미지 저장
        List<ProductImage> images = new ArrayList<>();
        int order = 1;
        if (form.getProductImages() != null) {
            for (MultipartFile file : form.getProductImages()) {
                if (file != null && !file.isEmpty()) {
                    ProductImage pi = new ProductImage();
                    pi.setProduct(savedProduct);  // product 객체 직접 세팅
                    pi.setFileName(StringUtils.cleanPath(file.getOriginalFilename()));
                    pi.setFileSize(file.getSize());
                    pi.setMimeType(file.getContentType());
                    pi.setImageOrder(order++);
                    pi.setImageData(file.getBytes());
                    images.add(pi);
                }
            }
        }
        productImageSVC.saveAll(images);

        // 지도 코스포인트 저장
        List<ProductCoursePoint> coursePoints = new ArrayList<>();
        if (form.getCoursePoints() != null) {
            int pointOrder = 1;
            for (ProductCoursePointForm pointForm : form.getCoursePoints()) {
                ProductCoursePoint pcp = new ProductCoursePoint();
                pcp.setProduct(savedProduct);  // product 객체 직접 세팅
                pcp.setLatitude(pointForm.getLatitude());
                pcp.setLongitude(pointForm.getLongitude());
                pcp.setDescription(pointForm.getDescription());
                pcp.setPointOrder(pointOrder++);
                pcp.setCreatedAt(new Date(System.currentTimeMillis()));
                coursePoints.add(pcp);
            }
        }
        productCoursePointSVC.saveAll(coursePoints);

        return "redirect:/product/list";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable("id") Long id, Model model, HttpSession session, RedirectAttributes redirectAttrs) {
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) {
            redirectAttrs.addFlashAttribute("redirectAfterLogin", "/product/edit/" + id);
            return "redirect:/login";
        }

        Product product = productSVC.getProduct(id).orElseThrow(() -> new IllegalArgumentException("없는 상품입니다."));
        if (product.getMember() == null || !product.getMember().getMemberId().equals(loginMember.getMemberId())) {
            return "redirect:/accessDenied";
        }

        List<ProductImage> images = productImageSVC.findByProductId(id);
        List<ProductCoursePoint> coursePoints = productCoursePointSVC.findByProductId(id);

        ProductUploadForm form = toForm(product);

        model.addAttribute("nickname", loginMember.getNickname());
        model.addAttribute("product", form);
        model.addAttribute("images", images);
        model.addAttribute("coursePoints", coursePoints);
        return "product/product_update";
    }

    @PostMapping("/edit/{id}")
    public String editSubmit(@PathVariable Long id,
                             @ModelAttribute ProductUploadForm form,
                             Model model) throws IOException {
        if (form.getProductImages() != null && form.getProductImages().size() > 10) {
            model.addAttribute("errorMessage", "이미지는 최대 10장까지 업로드 가능합니다.");
            return "product/edit";
        }

        Product product = toEntity(form);
        product.setProductId(id);
        product.setUpdateDate(new Date(System.currentTimeMillis()));

        productSVC.updateProduct(product);

        // 기존 이미지 및 코스포인트 삭제 후 재등록
        productImageSVC.deleteByProductId(id);
        List<ProductImage> images = new ArrayList<>();
        int order = 1;
        if (form.getProductImages() != null) {
            for (MultipartFile file : form.getProductImages()) {
                if (file != null && !file.isEmpty()) {
                    ProductImage pi = new ProductImage();
                    pi.setProduct(product);  // product 객체 직접 세팅
                    pi.setFileName(StringUtils.cleanPath(file.getOriginalFilename()));
                    pi.setFileSize(file.getSize());
                    pi.setMimeType(file.getContentType());
                    pi.setImageOrder(order++);
                    pi.setImageData(file.getBytes());
                    images.add(pi);
                }
            }
        }
        productImageSVC.saveAll(images);

        productCoursePointSVC.deleteByProductId(id);
        List<ProductCoursePoint> coursePoints = new ArrayList<>();
        if (form.getCoursePoints() != null) {
            int pointOrder = 1;
            for (ProductCoursePointForm pointForm : form.getCoursePoints()) {
                ProductCoursePoint pcp = new ProductCoursePoint();
                pcp.setProduct(product);  // product 객체 직접 세팅
                pcp.setLatitude(pointForm.getLatitude());
                pcp.setLongitude(pointForm.getLongitude());
                pcp.setDescription(pointForm.getDescription());
                pcp.setPointOrder(pointOrder++);
                pcp.setCreatedAt(new Date(System.currentTimeMillis()));
                coursePoints.add(pcp);
            }
        }
        productCoursePointSVC.saveAll(coursePoints);

        return "redirect:/product/view/" + id;
    }

    @GetMapping("/view/{id}")
    public String view(@PathVariable("id") Long id, Model model, HttpSession session) {
        Product product = productSVC.getProduct(id)
            .orElseThrow(() -> new IllegalArgumentException("상품이 없습니다."));
        List<ProductImage> images = productImageSVC.findByProductId(id);
        List<ProductCoursePoint> coursePoints = productCoursePointSVC.findByProductId(id);
        Member loginMember = (Member) session.getAttribute("loginMember");
        Long memberId = loginMember.getMemberId();
        Optional<SellerPage> optional = sellerPageSVC.findByMemberId(memberId);
        if (optional.isEmpty()) {
            return "redirect:/mypage/seller/create";
        }
        SellerPage sellerPage = optional.get();
        product.setProductImages(images);
        log.info("sellerId= {}", sellerPage.getMemberId());
        log.info("loginSeller = {}", sellerPage.getIntro());
        log.info("nickname = {}", sellerPage.getNickname());
        log.info("id = {}", memberId);

        model.addAttribute("nickname", sellerPage.getNickname());
        model.addAttribute("intro", sellerPage.getIntro());
        model.addAttribute("sellerImage", sellerPage.getImage());
        model.addAttribute("countProduct", productSVC.countByMemberId(memberId));
        model.addAttribute("product", product);
        model.addAttribute("images", images);
        model.addAttribute("coursePoints", coursePoints);

        return "product/productDetails";
    }

    // DTO -> Entity 변환 메서드
    private Product toEntity(ProductUploadForm form) throws IOException {
        Product product = new Product();

        // Member 객체 생성 후 세팅
        Member member = new Member();
        member.setMemberId(form.getMemberId());
        product.setMember(member);

        product.setCategory(form.getCategory());
        product.setTitle(form.getTitle());
        product.setGuideYn(form.getGuideYn());
        product.setNormalPrice(form.getNormalPrice());
        product.setGuidePrice(form.getGuidePrice());
        product.setSalesPrice(form.getSalesPrice());
        product.setSalesGuidePrice(form.getSalesGuidePrice());
        product.setTotalDay(form.getTotalDay());
        product.setTotalTime(form.getTotalTime());
        product.setReqMoney(form.getReqMoney());
        product.setSleepInfo(form.getSleepInfo());
        product.setTransportInfo(form.getTransportInfo());
        product.setFoodInfo(form.getFoodInfo());
        product.setReqPeople(form.getReqPeople());
        product.setTarget(form.getTarget());
        product.setStucks(form.getStucks());
        product.setDescription(form.getDescription());
        product.setDetail(form.getDetail());
        product.setPriceDetail(form.getPriceDetail());
        product.setGpriceDetail(form.getGpriceDetail());
        product.setStatus(
            (form.getStatus() == null || form.getStatus().isBlank()) ? "판매중" : form.getStatus()
        );

        // 첨부 문서 파일 처리
        MultipartFile docFile = form.getDocumentFile();
        if (docFile != null && !docFile.isEmpty()) {
            product.setFileName(docFile.getOriginalFilename());
            product.setFileType(docFile.getContentType());
            product.setFileSize(docFile.getSize());
            product.setFileData(docFile.getBytes());
        }

        return product;
    }

    // Entity -> DTO 변환 메서드
    private ProductUploadForm toForm(Product product) {
        ProductUploadForm form = new ProductUploadForm();

        form.setMemberId(product.getMember() != null ? product.getMember().getMemberId() : null);
        form.setCategory(product.getCategory());
        form.setTitle(product.getTitle());
        form.setGuideYn(product.getGuideYn());
        form.setNormalPrice(product.getNormalPrice());
        form.setGuidePrice(product.getGuidePrice());
        form.setSalesPrice(product.getSalesPrice());
        form.setSalesGuidePrice(product.getSalesGuidePrice());
        form.setTotalDay(product.getTotalDay());
        form.setTotalTime(product.getTotalTime());
        form.setReqMoney(product.getReqMoney());
        form.setSleepInfo(product.getSleepInfo());
        form.setTransportInfo(product.getTransportInfo());
        form.setFoodInfo(product.getFoodInfo());
        form.setReqPeople(product.getReqPeople());
        form.setTarget(product.getTarget());
        form.setStucks(product.getStucks());
        form.setDescription(product.getDescription());
        form.setDetail(product.getDetail());
        form.setPriceDetail(product.getPriceDetail());
        form.setGpriceDetail(product.getGpriceDetail());
        form.setStatus(product.getStatus());

        return form;
    }

}