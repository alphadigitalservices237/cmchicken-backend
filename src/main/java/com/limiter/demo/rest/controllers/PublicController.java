package com.limiter.demo.rest.controllers;
import com.limiter.demo.forms.CTA;
import com.limiter.demo.models.*;
import com.limiter.demo.repositories.*;
import org.json.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.limiter.demo.payment.Code;
import com.limiter.demo.payment.PaymentRequest;
import com.limiter.demo.payment.PaymentResponse;
import com.limiter.demo.payment.PaymentService;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import java.util.*;
import java.util.stream.Collectors;

import javax.mail.MessagingException;

import org.slf4j.LoggerFactory;
@RestController
@RequestMapping("api/public")
@CrossOrigin(origins = "*")

public class PublicController {
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private ReceiptRepository receiptRepository;
    @Autowired
    private CTARepository ctaRepository;
    @Value("${notchpay.api.url}")
    private String notchPayApiUrl;

    @Value("${notchpay.api.key}")
    private String notchPayApiKey;
    @Autowired
    private DeliveryRepository deliveryRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    PurchaseObjectRepo purchaseObjectRepo;
    @Autowired
    TemporaryObjectRepo temporaryObjectRepo;


    @Autowired
    private EmailService emailService;

    Code c1 = new Code();
    List<Product> tempo = new ArrayList<>();

    @GetMapping("product/all")
    public Object viewAllProducts() {
        return new ResponseEntity<>(productRepository.findAll().parallelStream().collect(Collectors.toList()), HttpStatus.OK);
    }

    private static final String API_URL = "https://api.notchpay.co/payments/initialize";
    private static final String VERIFY_URL = "https://api.notchpay.co/payments/";
    private static final Logger logger = LoggerFactory.getLogger(PublicController.class);
    @PostMapping("products/confirm")
    public Object confirmProducts(@RequestBody List<Product> products) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Optional<UserEntity> client = userRepository.findByUsername(auth.getName());
        List<Double> sums = new ArrayList<>();

        try {

           
            for(Product p: products)
            {
                TemporaryObject to = new TemporaryObject();
                to.setId(p.getId());
                to.setName(p.getName());
                to.setDescription(p.getDescription());
                to.setPrice(p.getPrice());
                to.setQuantity(p.getQuantity());
                to.setImage(p.getImage());
                temporaryObjectRepo.save(to);

                for(Product po : productRepository.findAll())
                {
                    po.setQuantity(po.getQuantity() - to.getQuantity());
                    tempo.add(po);
                }
    
            }
            

            System.out.println(temporaryObjectRepo.findAll());
            for (Product p : products) {
                sums.add(p.getPrice() * p.getQuantity());
            }
            double sum = 0;
            for (int i = 0; i < sums.size(); i++) {
                sum = sums.get(i) + sum;

            }
            return new ResponseEntity<>("SUM IS: " + sum, HttpStatus.OK);
        }
        catch (Exception ex)
        {
            logger.info("THE ERROR IS: "+ex.getMessage()+"\t"+ex.getLocalizedMessage());
            return new ResponseEntity<>(ex.getMessage(),HttpStatus.BAD_REQUEST);
        }
    }



@GetMapping("transaction/verify")
public String getPaymentStatus() {
    RestTemplate restTemplate = new RestTemplate();

    // Prepare headers
    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", notchPayApiKey);
    headers.set("Accept", "application/json");

    // Create request entity
    HttpEntity<String> requestEntity = new HttpEntity<>(headers);

    // Send GET request
    ResponseEntity<String> response = restTemplate.exchange(VERIFY_URL+c1.getContent(), HttpMethod.GET, requestEntity, String.class);
    System.out.println(VERIFY_URL+c1.getContent());

    // Return response body
    return response.getBody();
}
    public String updatePayment(String reference,String phone) //Method to complete the payment
     {

        try {
            RestTemplate restTemplate = new RestTemplate();

            // Prepare headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            headers.set("Authorization", notchPayApiKey);

            // Prepare request body
            ObjectMapper mapper = new ObjectMapper();
            String jsonBody = "{ \"channel\": \"cm.mobile\", \"data\" : { \"phone\": \"+237699189765\" } }";
            // Parse the JSON string into a JSONObject
            JSONObject jsonObject = new JSONObject(jsonBody);

            // Navigate to the 'data' object and then to the 'phone' field
            JSONObject dataObject = jsonObject.getJSONObject("data");

            // Update the 'phone' field with the new phone number
            dataObject.put("phone", phone);

            // Convert the modified JSONObject back to a string
            String updatedJsonBody = jsonObject.toString();

            System.out.println("Updated JSON: " + updatedJsonBody);
            HttpEntity<String> requestEntity = new HttpEntity<>(updatedJsonBody, headers);

            // Send PUT request
            ResponseEntity<String> response = restTemplate.exchange(VERIFY_URL+reference, HttpMethod.PUT, requestEntity, String.class);
            // Return response body
            return response.getBody();
        } catch (Exception e) {
           return  e.getMessage()+e.getLocalizedMessage();
        }
//        String jsonBody = "{ \"channel\" : \"cm.orange\" , data : { phone: +237699189765 } }";

        // Create request entity

    }

    public String cancelPayment()  //method to cancel payment and refund user
    {
        RestTemplate restTemplate = new RestTemplate();

        // Prepare headers
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", notchPayApiKey);
        headers.set("Accept", "application/json");

        // Create request entity
        HttpEntity<String> requestEntity = new HttpEntity<>(headers);

        // Send GET request
        ResponseEntity<String> response = restTemplate.exchange(VERIFY_URL+c1.getContent(), HttpMethod.DELETE, requestEntity, String.class);
        System.out.println("URL IS: "+VERIFY_URL+c1.getContent());

        // Return response body
        return response.getBody();


    }

@PostMapping("product/buy")
public Object doAll(@RequestBody List<Product> products,
                    @RequestParam("email") String email,
                    @RequestParam("currency") String currency,
                    @RequestParam("amount") int amount,
                    @RequestParam("phone") String phone,
                    @RequestParam("reference") String reference,
                    @RequestParam("description") String description,
                    @RequestParam("location") String location

                    ) throws JsonProcessingException
{
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    Optional<UserEntity> user = userRepository.findByUsername(auth.getName());
    List<Double> sums = new ArrayList<>();

        for (Product p : products) {
            sums.add(p.getPrice() * p.getQuantity());
        }
        double sum = 0;
        for (int i = 0; i < sums.size(); i++) {
            sum = sums.get(i) + sum;
            logger.info("THE SUM IS: "+sum);
        }
    System.out.println("SUM IS: " + sum);
//        return new ResponseEntity<>("SUM IS: " + sum, HttpStatus.OK);
    if(user.isPresent())
    {
        RestTemplate restTemplate = new RestTemplate();

        // Prepare headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Authorization", notchPayApiKey);
        headers.set("Accept", "application/json");

        // Prepare form data
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("email", email);
        formData.add("currency", currency);
        formData.add("amount", String.valueOf(amount));
        formData.add("phone", phone);
        formData.add("reference", reference);
        formData.add("description", description);

        // Create request entity
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(formData, headers);

        // Send POST request
        ResponseEntity<String> response = restTemplate.postForEntity(API_URL, requestEntity, String.class);
        Map<Object, Object> map = new HashMap<>();
        ObjectMapper mapper = new ObjectMapper();

        JsonNode root = mapper.readTree(response.getBody());
        JsonNode referenceNode = root.path("transaction").path("reference");
        String ref = referenceNode.asText();

       /* String[] elements = response.getBody().split(":");
        String[] value = elements[25].split(",");
        String code = value[0].replaceAll("^\"|\"$|\\\"", "");*/
        c1.setContent(ref);
        c1.setReference(ref);
        logger.info("THE API REFERENCE: "+c1.getContent());


        map.put(getPaymentStatus(), updatePayment(c1.getContent(),phone));

        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {


                try {
                    try {
                        doTheRest(products,user,location,phone);
                    } catch (MessagingException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }

            }

        };
        long delay = 70 * 1000;
        logger.info("ACTION WILL COMPLETE IN 70 SECONDS PLEASE");//20 seconds in milliseconds
        timer.schedule(task, delay);

        // Return response body

        return new ResponseEntity<>("PLEASE VALIDATE ON YOUR PHONE",HttpStatus.CREATED);
    }
    return new ResponseEntity<>("Please login",HttpStatus.UNAUTHORIZED);

    }

    @PostMapping("product/buy-with-delivery")
    public Object doAllWithDelivery(@RequestBody List<Product> products,
                        @RequestParam("email") String email,
                        @RequestParam("currency") String currency,
                        @RequestParam("amount") int amount,
                        @RequestParam("phone") String phone,
                        @RequestParam("reference") String reference,
                        @RequestParam("description") String description,
                        @RequestParam ("location")String destination
                        ) throws JsonProcessingException
    {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Optional<UserEntity> user = userRepository.findByUsername(auth.getName());
        List<Double> sums = new ArrayList<>();

        for (Product p : products) {
            sums.add(p.getPrice() * p.getQuantity());
        }
        double sum = 0;
        for (int i = 0; i < sums.size(); i++) {
            sum = sums.get(i) + sum;
            logger.info("THE SUM IS: "+sum);
        }
        System.out.println("SUM IS: " + sum);
//        return new ResponseEntity<>("SUM IS: " + sum, HttpStatus.OK);
        if(user.isPresent())
        {
            RestTemplate restTemplate = new RestTemplate();

            // Prepare headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.set("Authorization", notchPayApiKey);
            headers.set("Accept", "application/json");

            // Prepare form data
            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("email", email);
            formData.add("currency", currency);
            formData.add("amount", String.valueOf(amount));
            formData.add("phone", phone);
            formData.add("reference", reference);
            formData.add("description", description);

            // Create request entity
            HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(formData, headers);

            // Send POST request
            ResponseEntity<String> response = restTemplate.postForEntity(API_URL, requestEntity, String.class);
            Map<Object, Object> map = new HashMap<>();
            ObjectMapper mapper = new ObjectMapper();

            JsonNode root = mapper.readTree(response.getBody());
            JsonNode referenceNode = root.path("transaction").path("reference");
            String ref = referenceNode.asText();

       /* String[] elements = response.getBody().split(":");
        String[] value = elements[25].split(",");
        String code = value[0].replaceAll("^\"|\"$|\\\"", "");*/
            c1.setContent(ref);
            c1.setReference(ref);
            logger.info("THE API REFERENCE: "+c1.getContent());


            map.put(getPaymentStatus(), updatePayment(c1.getContent(),phone));

            Timer timer = new Timer();
            TimerTask task = new TimerTask() {
                @Override
                public void run() {

                    try {
                        try {
                            doTheRestWithDelivery(products,user,destination,phone);
                        } catch (MessagingException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                }

            };
            long delay = 70 * 1000;
            logger.info("ACTION WILL COMPLETE IN 70 SECONDS PLEASE");//70 seconds in milliseconds
            timer.schedule(task, delay);

            // Return response body

            return new ResponseEntity<>("PLEASE VALIDATE ON YOUR PHONE",HttpStatus.CREATED);
        }
        return new ResponseEntity<>("Please login",HttpStatus.UNAUTHORIZED);

    }
    public Object doTheRest(List<Product> items,Optional<UserEntity> user, String location, String phone_number) throws JsonProcessingException, MessagingException
    {
        String jsonString=getPaymentStatus();
        ObjectMapper Mapper = new ObjectMapper();

            JsonNode rootNode = Mapper.readTree(jsonString);
            JsonNode statusNode = rootNode.path("transaction").path("status");
            String statusValue = statusNode.asText();
            System.out.println("the status is: "+statusValue);
            if(statusValue.equals("pending") || statusValue.equals("failed") || statusValue.equals("expired") || statusValue.equals("processing") || statusValue.equals("failed"))
            {
                // I have to produce code to cancel the action
                cancelPayment();
                System.out.println("ACTION FAILED AFTER 70 SECONDS");
                logger.info("Payment was not successful");
                return new ResponseEntity<>("Payment was not successful",HttpStatus.BAD_GATEWAY);

            }
            else {
                    Receipt receipt =new Receipt();
                    List<Purchaseobject> objects = new ArrayList<>();
                                    for (Product  t: items)
                                     {

                    Purchaseobject po = new Purchaseobject();
                    po.setName(t.getName());
                    po.setBought(true);
                    po.setUser_id(user.get().getId());
                    po.setDescription(t.getDescription());
                    po.setQuantity(t.getQuantity());
                    po.setBought(true);
                    po.setAddedDate(new Date());
                    po.setPrice(t.getPrice());
                    po.setImage(t.getImage());
                    objects.add(po);
                    purchaseObjectRepo.save(po);
                    
                    // receipt.getPurchasedObjects().add(po);
                    
                    logger.info("Object saved to database and receipt created");
                }
                
                productRepository.saveAll(tempo);
                receipt.setDate(new Date());
                receipt.setUser_id(user.get().getId());
                receipt.setPurchasedObjects(objects);
                receipt.setUser_name(user.get().getUsername());
                receipt.setLocation(location);
                receipt.setPhone_number(phone_number);
                receiptRepository.save(receipt);
                emailService.sendEmail(user.get().getUsername(), "CM CHICKEN PURCHASE", "<HTML><body><h1>Sucessfully Made a payment of items: "+objects+"</h1></body></HTML>");
                objects.clear();
                System.out.println(items);
                logger.info("Payment successful");
                return new ResponseEntity<>("Payment successful",HttpStatus.OK);
            }

    }

    public Object doTheRestWithDelivery(List<Product> items,Optional<UserEntity> user,String destination,String phone_number) throws JsonProcessingException, MessagingException
    {
        String jsonString=getPaymentStatus();
        ObjectMapper Mapper = new ObjectMapper();

        JsonNode rootNode = Mapper.readTree(jsonString);
        JsonNode statusNode = rootNode.path("transaction").path("status");
        String statusValue = statusNode.asText();
        System.out.println("the status is: "+statusValue);
        if(statusValue.equals("pending") || statusValue.equals("failed") || statusValue.equals("expired"))
        {
            // I have to produce code to cancel the action
            cancelPayment();
            System.out.println("ACTION FAILED AFTER 70 SECONDS");
            logger.info("Payment was not successful");
            return new ResponseEntity<>("Payment was not successful",HttpStatus.BAD_GATEWAY);

        }
        else {
Delivery delivery = new Delivery();
Receipt receipt =new Receipt();
List<Purchaseobject> objects = new ArrayList<>();
                for (Product  t: items) {

                    Purchaseobject po = new Purchaseobject();
                    po.setName(t.getName());
                    po.setBought(true);
                    po.setUser_id(user.get().getId());
                    po.setDescription(t.getDescription());
                    po.setQuantity(t.getQuantity());
                    po.setBought(true);
                    po.setAddedDate(new Date());
                    po.setPrice(t.getPrice());
                    po.setImage(t.getImage());
                    objects.add(po);
                    purchaseObjectRepo.save(po);
                    
                    // receipt.getPurchasedObjects().add(po);
                    
                    logger.info("Object saved to database and receipt created");
            }
            productRepository.saveAll(tempo);
            receipt.setDate(new Date());
            receipt.setUser_id(user.get().getId());
            receipt.setPurchasedObjects(objects);
            receipt.setUser_name(user.get().getUsername());
            receipt.setLocation(destination);
            receipt.setPhone_number(phone_number);
            receipt.setDelivered(true);
            receiptRepository.save(receipt);
            objects.clear();
            System.out.println(items);
            emailService.sendEmail(user.get().getUsername(), "CM CHICKEN PURCHASE", "<HTML><body><h1>Sucessfully Made a payment of items: "+objects+"</h1></body></HTML>");
            logger.info("Payment successful");
            return new ResponseEntity<>("Payment successful",HttpStatus.OK);
        }

    }
    @PostMapping("form/cta/confirmation")
    public Object sendCtaForm(@RequestBody List<Product> menus,
                              @RequestParam int personNumber,
                              @RequestParam String number,
                              @RequestParam Date date,
                              @RequestParam String location)
    {
        try {
            CTA cta = new CTA();
            cta.setDate(date);
            cta.setLocation(location);
            cta.setNumber(number);
            cta.setPersonNumber(personNumber);
            cta.getMenus().addAll(menus.stream().collect(Collectors.toList()));
            ctaRepository.save(cta);
            return new ResponseEntity<>("DATA SAVED CORRECTLY",HttpStatus.OK);
        }
        catch(Exception exception)
        {
            return new ResponseEntity<>("COULD NOT SAVE DATA BECAUSE "+exception.getLocalizedMessage(),HttpStatus.BAD_REQUEST);
        }
    }
}



