package com.limiter.demo.rest.controllers;
import com.limiter.demo.models.Availability;
import com.limiter.demo.models.Category;
import com.limiter.demo.models.Product;
import com.limiter.demo.models.UserEntity;
import com.limiter.demo.repositories.CategoryRepository;
import com.limiter.demo.repositories.ProductRepository;
import com.limiter.demo.repositories.PurchaseObjectRepo;
import com.limiter.demo.repositories.UserRepository;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.tomcat.util.http.fileupload.impl.SizeLimitExceededException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import javax.imageio.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.apache.commons.imaging.*;

@RestController
@RequestMapping("api/v1/auth")
@CrossOrigin(origins = "*")

public class ProductController {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @Autowired
    ProductController( ProductRepository productRepository, CategoryRepository categoryRepository) {

        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;

    }

    public byte[] compressImage(byte[] imageBytes, float compressionQuality, String format) throws IOException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(imageBytes);
        BufferedImage image = ImageIO.read(inputStream);

        // Choose an appropriate image format (e.g., JPEG, PNG)

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageWriter writer = ImageIO.getImageWritersByFormatName(format).next();
        ImageWriteParam param = writer.getDefaultWriteParam();
        param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        param.setCompressionQuality(compressionQuality);

        // Prepare image output stream
        ImageIO.write(image, format, outputStream);

        return outputStream.toByteArray();
    }

    public byte[] resizeImage
            (
                    byte[] originalImage,
                    int targetWidth,
                    int targetHeight,
                    String format) throws Exception {
        /*BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageBytes));
        BufferedImage resizedImage = ImageUtils.scale(image, targetWidth, targetHeight);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(resizedImage, "jpg", outputStream);
        return outputStream.toByteArray();*/
        ByteArrayInputStream inputStream = new ByteArrayInputStream(originalImage);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Thumbnails.of(inputStream)
                .size(targetWidth, targetHeight)
                .outputFormat(format)
                .outputQuality(0.7f)
                .toOutputStream(outputStream);
        return outputStream.toByteArray();
    }

    @PostMapping("product/{quantity}/create")
    public Object addProduct
            (@PathVariable int quantity,
             @RequestParam("name") String name,
             @RequestParam("image") MultipartFile image,
             @RequestParam("price") double price,
             @RequestParam("description") String description,
             @RequestParam("category_id") long cat_id) throws Exception {
        Product p = new Product();
        p.setAddedDate(new Date());
        p.setDescription(description);
        p.setQuantity(quantity);
        p.setPrice(Double.valueOf(price));
        p.setName(name);
        p.setCategory_id(cat_id);

       /* if (image.isEmpty() || image == null)
        {
            return new ResponseEntity<>("No image, Image is required",HttpStatus.BAD_REQUEST);
        }*/

        if (image.getSize() > 5 * 1024 * 1024) {
            // Image size exceeds limit of 2MB
            return new ResponseEntity<>("Image size cannot exceed 5MB", HttpStatus.BAD_REQUEST);
        }
        if (image.isEmpty() || image == null) {
            try {
                p.setAddedDate(new Date());
                p.setDescription(description);
                p.setQuantity(quantity);
                p.setPrice(Double.valueOf(price));
                p.setName(name);
                p.setCategory_id(cat_id);
                productRepository.save(p);
                return new ResponseEntity<>("Product created with no image", HttpStatus.CREATED);
            } catch (Exception ex) {
                return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
            }
        } else if (image.getSize() < 2 * 1024 * 1024) {
            if (image.getContentType().equals("image/jpg") ||
                    image.getContentType().equals("image/jpeg") ||
                    image.getContentType().equals("image/png")) {
//                p.setImage(compressImage(image.getBytes(),0.7f,image.getContentType().substring(6)));
                p.setImage(resizeImage(compressImage(image.getBytes(), 0.55f, image.getContentType().substring(6)), 500, 350, image.getContentType().substring(6)));
                productRepository.save(p);

                return new ResponseEntity<>("Product saved", HttpStatus.CREATED);
            }
            return new ResponseEntity<>("MEDIA TYPE NOT SUPPORTED", HttpStatus.BAD_REQUEST);


        }

        return null;
    }

    @PutMapping("product/{id}/{quantity}/increment")
    public Object incrementProduct
            (@PathVariable int quantity,
             @PathVariable long id
            ) {
        Optional<Product> product = productRepository.findById(id);
        if (product.isPresent()) {
            product.get().setQuantity(quantity + product.get().getQuantity());
            productRepository.save(product.get());
            return new ResponseEntity<>("Product Quantity incremented", HttpStatus.OK);
        }

        return new ResponseEntity<>("COULD NOT FIND PRODUCT", HttpStatus.BAD_REQUEST);
    }

    @PutMapping("product/{id}/{quantity}/decrement")
    public Object decrementProduct
            (@PathVariable int quantity,
             @PathVariable long id
            ) {
        Optional<Product> product = productRepository.findById(id);
        if (product.isPresent()) {
            if (quantity > product.get().getQuantity()) {
                return new ResponseEntity<>("CANNOT REMOVE MORE THAN PRESENT QUANTITY", HttpStatus.BAD_REQUEST);
            } else if (quantity <= product.get().getQuantity()) {
                product.get().setQuantity(product.get().getQuantity() - quantity);
                productRepository.save(product.get());
                return new ResponseEntity<>("Product Quantity decremented", HttpStatus.OK);
            }


        }

        return new ResponseEntity<>("COULD NOT FIND PRODUCT", HttpStatus.BAD_REQUEST);
    }

    @PutMapping("product/{id}/update")
    public Object modifyProduct(@PathVariable long id,
                                @RequestParam("description") String description,
                                @RequestParam("quantity") int quantity,
                                @RequestParam("price") double price,
                                @RequestParam("image") MultipartFile image,
                                @RequestParam("name") String name) throws IOException {
        Optional<Product> product = productRepository.findById(id);
        if (product.isPresent()) {
            product.get().setPrice(price);
            product.get().setDescription(description);
            product.get().setQuantity(quantity);
            product.get().setName(name);
            product.get().setImage(image.getBytes());
            productRepository.save(product.get());
            return new ResponseEntity<>("Product " + product.get().getName() + "updated", HttpStatus.OK);
        }
        return new ResponseEntity<>("UNABLE TO FIND PRODUCT", HttpStatus.BAD_REQUEST);

    }


    @DeleteMapping("product/{id}/delete")
    public Object deleteProduct(@PathVariable long id) throws IOException
//
    {
        Optional<Product> product = productRepository.findById(id);
        if (product.isPresent()) {
            productRepository.deleteById(id);
            return new ResponseEntity<>("Product " + product.get().getName() + "deleted", HttpStatus.OK);
        }
        return new ResponseEntity<>("UNABLE TO FIND PRODUCT, CANNOT DELETE IT", HttpStatus.BAD_REQUEST);

    }

@PostMapping("category/create")
    public Object createCategory(@RequestParam("name") String name)
{
    try {
        Category category = new Category();
        category.setName(name);
        categoryRepository.save(category);
        return new ResponseEntity<>("category\t"+name+"\tcreated",HttpStatus.OK);
    }
   catch(Exception exception)
   {
       return new ResponseEntity<>("could not create category",HttpStatus.BAD_REQUEST);
   }

}
@GetMapping("category/{id}/products")
@Transactional
    public Object getCategoryProducts(@PathVariable long id)
{
        Optional<Category> category = categoryRepository.findById(id);
        if(category.isPresent())
        {
                return new ResponseEntity<>(category.get().getProducts().parallelStream().collect(Collectors.toList()), HttpStatus.OK);

        } else {
            return new ResponseEntity<>("CATEGORY DOES NOT EXIST",HttpStatus.BAD_REQUEST);
        }

}

    @GetMapping("categories/all")
    public Object getCategories()
    {
        return  categoryRepository.findAll().parallelStream().collect(Collectors.toList());

    }
    @GetMapping("product/{name}/all")
    public Object getProductsLikeName(@PathVariable String name)
    {
        List<Product> products = productRepository.findAll().parallelStream().filter(prod->prod.getName().contains(name)).collect(Collectors.toList());
        return new ResponseEntity<>(products,HttpStatus.OK);
    }
}
