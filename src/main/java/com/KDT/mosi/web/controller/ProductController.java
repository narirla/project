package com.KDT.mosi.web.controller;

import com.KDT.mosi.domain.entity.*;
import com.KDT.mosi.domain.mypage.seller.svc.SellerPageSVC;
import com.KDT.mosi.domain.product.svc.ProductCoursePointSVC;
import com.KDT.mosi.domain.product.svc.ProductImageSVC;
import com.KDT.mosi.domain.product.svc.ProductSVC;
import com.KDT.mosi.web.form.product.*;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.sql.Date;
import java.util.*;

@Slf4j
@Controller
@RequestMapping("/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductSVC productSVC;
    private final ProductImageSVC productImageSVC;
    private final ProductCoursePointSVC productCoursePointSVC;
    private final SellerPageSVC sellerPageSVC;

    // 구매자 상품 조회
    @GetMapping("/list")
    public String list(Model model, HttpSession session,
                       HttpServletRequest request,
                       @RequestParam(name = "page", defaultValue = "1") int page,
                       @RequestParam(name = "size", defaultValue = "12") int size,
                       @RequestParam(name = "category", defaultValue = "area") String category){

        List<Product> products = productSVC.getProductsByPage(page, size);
        long totalCount = productSVC.countAllProducts();

        int totalPages = (int) Math.ceil((double) totalCount / size);

        List<ProductListForm> listForms = new ArrayList<>();
        for (Product product : products) {
            List<ProductImage> images = productImageSVC.findByProductId(product.getProductId());
            List<ProductCoursePoint> coursePoints = productCoursePointSVC.findByProductId(product.getProductId());

            ProductListForm form = new ProductListForm();
            form.setProduct(product);
            form.setImages(images);
            form.setCoursePoints(coursePoints);

            listForms.add(form);
        }

        return "product/product_list";
    }

    // 판매자별 등록 상품 조회
    @GetMapping("/manage")
    public String manage(Model model, HttpSession session,
                       HttpServletRequest request,
                       @RequestParam(name = "page", defaultValue = "1") int page,
                       @RequestParam(name = "size", defaultValue = "5") int size,
                       @RequestParam(name = "status", required = false, defaultValue = "all") String status) {  // status 필수 파라미터


        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) {
            throw new IllegalStateException("로그인한 회원이 아닙니다.");
        }
        Long memberId = loginMember.getMemberId();

        // status 값은 판매중 또는 판매대기로 반드시 들어온다고 가정
        List<Product> products;
        long totalCount;

        if ("all".equals(status)) {
            // 전체 목록 조회 (상태 조건 없음)
            products = productSVC.getProductsByMemberIdAndPage(memberId, page, size);
            totalCount = productSVC.countByMemberId(memberId);
        } else if ("판매중".equals(status) || "판매대기".equals(status)) {
            // 상태 필터링
            products = productSVC.getProductsByMemberIdAndStatusAndPage(memberId, status, page, size);
            totalCount = productSVC.countByMemberIdAndStatus(memberId, status);
        } else {
            // 유효하지 않은 status 값은 기본값으로 변경 or 에러 처리
            status = "판매중";
            products = productSVC.getProductsByMemberIdAndStatusAndPage(memberId, status, page, size);
            totalCount = productSVC.countByMemberIdAndStatus(memberId, status);
        }

        int totalPages = (int) Math.ceil((double) totalCount / size);

        Optional<SellerPage> optionalSellerPage = sellerPageSVC.findByMemberId(memberId);
        if (optionalSellerPage.isEmpty()) {
            return "redirect:/mypage/seller/create";
        }
        SellerPage sellerPage = optionalSellerPage.get();

        byte[] imageBytes = sellerPage.getImage();
        String base64SellerImage = null;
        if (imageBytes != null && imageBytes.length > 0) {
            base64SellerImage = "data:image/png;base64," + Base64.getEncoder().encodeToString(imageBytes);
        }

        List<ProductManagingForm> managingForms = new ArrayList<>();
        for (Product product : products) {
            List<ProductImage> images = productImageSVC.findByProductId(product.getProductId());
            List<ProductCoursePoint> coursePoints = productCoursePointSVC.findByProductId(product.getProductId());

            ProductManagingForm form = new ProductManagingForm();
            form.setProduct(product);
            form.setImages(images);
            form.setCoursePoints(coursePoints);

            managingForms.add(form);
        }

        // 디버깅용 출력
        Object csrfObj1 = request.getAttribute(CsrfToken.class.getName());
        Object csrfObj2 = request.getAttribute("_csrf");
        System.out.println("CSRF 토큰 객체1: " + csrfObj1);
        System.out.println("CSRF 토큰 객체2: " + csrfObj2);

        CsrfToken csrfToken = null;
        if (csrfObj1 instanceof CsrfToken) {
            csrfToken = (CsrfToken) csrfObj1;
        } else if (csrfObj2 instanceof CsrfToken) {
            csrfToken = (CsrfToken) csrfObj2;
        }
        if (csrfToken != null) {
            model.addAttribute("_csrf", csrfToken);
        } else {
            System.out.println("CSRF 토큰을 찾을 수 없습니다.");
        }

        model.addAttribute("_csrf", csrfToken);
        model.addAttribute("productManagingForms", managingForms);
        model.addAttribute("nickname", sellerPage.getNickname());
        model.addAttribute("sellerImage", base64SellerImage);
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("selectedStatus", status);  // 뷰 선택값 유지용

        log.info("nickname = {}", sellerPage);

        return "product/product_managing";
    }

    // selectbox 값 변경에 따라 DB status값 변경
    @PatchMapping("/status/{productId}")
    @ResponseBody
    public ResponseEntity<?> updateStatus(@PathVariable(name = "productId") Long productId, @RequestBody Map<String, String> body) {
        String status = body.get("status");

        // 상태 값 유효성 검사
        if (status == null || (!"판매중".equals(status) && !"판매대기".equals(status))) {
            return ResponseEntity.badRequest().body("잘못된 상태 값입니다.");
        }

        try {
            productSVC.updateProductStatus(productId, status);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException iae) {
            // 예: 존재하지 않는 상품 아이디 처리
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(iae.getMessage());
        } catch (Exception e) {
            e.printStackTrace(); // 서버 로그에 스택트레이스 출력
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("상태 업데이트 실패");
        }
    }

    // 상품 등록 페이지 호출
    @GetMapping("/upload")
    public String uploadForm(Model model, HttpSession session, RedirectAttributes redirectAttrs) {

        Member loginMember = (Member) session.getAttribute("loginMember");

        if (loginMember == null) {
            redirectAttrs.addFlashAttribute("redirectAfterLogin", "/product/upload");
            return "redirect:/login";
        }
        System.out.println(loginMember.getNickname());

        model.addAttribute("nickname", loginMember.getNickname());
        model.addAttribute("productUploadForm", new ProductUploadForm());
        return "product/product_enroll";
    }
    // 상품 등록 적용
    @PostMapping("/upload")
    public String uploadSubmit(@ModelAttribute ProductUploadForm form, HttpSession session ,Model model) throws IOException {

        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) {
            return "redirect:/login";
        }

        // 로그인한 회원 ID를 form에 세팅
        form.setMemberId(loginMember.getMemberId());
        form.setNickname(loginMember.getNickname());

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

        return "redirect:/product/manage";
    }

    // 수정 페이지 호출
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
    // 수정 적용
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

    // 상세페이지
    @GetMapping("/view/{id}")
    public String view(@PathVariable("id") Long id, Model model, HttpSession session) {

        // 3) 로그인 회원 정보 조회
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) {
            // 필요 시 로그인 페이지로 리다이렉트 또는 예외 처리
            throw new IllegalStateException("로그인한 회원이 아닙니다.");
        }
        Long memberId = loginMember.getMemberId();

        // 1) 상품 조회, 없으면 예외 처리
        Product product = productSVC.getProduct(id)
            .orElseThrow(() -> new IllegalArgumentException("상품이 없습니다."));

        // 2) 상품 이미지, 코스 포인트 리스트 조회
        List<ProductImage> images = productImageSVC.findByProductId(id);
        List<ProductCoursePoint> coursePoints = productCoursePointSVC.findByProductId(id);

        // 4) 판매자 페이지 정보 조회
        Optional<SellerPage> optional = sellerPageSVC.findByMemberId(memberId);
        if (optional.isEmpty()) {
            return "redirect:/mypage/seller/create";
        }
        SellerPage sellerPage = optional.get();

        // 5) DTO에 데이터 세팅
        product.setMember(loginMember);
        product.setProductImages(images); // 엔티티에 이미지 세팅

        ProductDetailForm productDetailForm = new ProductDetailForm();
        productDetailForm.setProduct(product);
        productDetailForm.setImages(images);
        productDetailForm.setCoursePoints(coursePoints);
        productDetailForm.setNickname(sellerPage.getNickname());
        productDetailForm.setIntro(sellerPage.getIntro());

        // 이미지 byte[] -> base64로 인코딩(Null 체크 필수)
        byte[] imageBytes = sellerPage.getImage();
        if (imageBytes != null && imageBytes.length > 0) {
            String base64Image = "data:image/png;base64," + Base64.getEncoder().encodeToString(imageBytes);
            productDetailForm.setSellerImage(base64Image);
        } else {
            productDetailForm.setSellerImage(null);
        }

        // 판매자 상품 수
        productDetailForm.setCountProduct(productSVC.countByMemberId(memberId));

        // 6) model에 DTO 등록
        model.addAttribute("productDetailForm", productDetailForm);
        log.info("nickname = {}", sellerPage.getNickname());

        return "product/product_detail";
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