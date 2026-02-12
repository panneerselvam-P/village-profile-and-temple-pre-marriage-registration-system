package com.temple.marriage.service;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.temple.marriage.model.MarriageRegistration;
import com.temple.marriage.repository.MarriageRegistrationRepository;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.temple.marriage.repository.UserRepository;
import java.util.List;
import java.util.UUID;

@Service
public class MarriageRegistrationService {

    @Autowired
    private MarriageRegistrationRepository repository;

    @Autowired
    private UserRepository userRepository;

    @Value("${razorpay.key.id}")
    private String razorpayKeyId;

    @Value("${razorpay.key.secret}")
    private String razorpayKeySecret;

    public MarriageRegistration saveRegistration(MarriageRegistration registration) {
        if (registration.getApplicationNumber() == null || registration.getApplicationNumber().isEmpty()) {
            if (registration.getGroomPhone() != null && !registration.getGroomPhone().isEmpty()) {
                userRepository.findByMobileNumber(registration.getGroomPhone())
                        .ifPresent(u -> registration.setApplicationNumber(u.getApplicationNumber()));
            }
            if (registration.getApplicationNumber() == null && registration.getBridePhone() != null
                    && !registration.getBridePhone().isEmpty()) {
                userRepository.findByMobileNumber(registration.getBridePhone())
                        .ifPresent(u -> registration.setApplicationNumber(u.getApplicationNumber()));
            }
        }
        return repository.save(registration);
    }

    public String createRazorpayOrder(double amount) throws Exception {
        RazorpayClient razorpay = new RazorpayClient(razorpayKeyId, razorpayKeySecret);

        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", amount * 100); // Amount in paise
        orderRequest.put("currency", "INR");
        orderRequest.put("receipt", "txn_" + UUID.randomUUID().toString());
        orderRequest.put("payment_capture", 1);

        Order order = razorpay.orders.create(orderRequest);
        return order.get("id");
    }

    public void updatePaymentStatus(Long registrationId, String paymentId, String status) {
        MarriageRegistration registration = repository.findById(registrationId).orElse(null);
        if (registration != null) {
            registration.setRazorpayPaymentId(paymentId);
            registration.setPaymentStatus(status);
            repository.save(registration);
        }
    }

    public MarriageRegistration getRegistration(Long id) {
        return repository.findById(id).map(this::enrichApplicationNumber).orElse(null);
    }

    public List<MarriageRegistration> getAllRegistrations() {
        List<MarriageRegistration> list = repository.findAll();
        for (MarriageRegistration reg : list) {
            String oldVal = reg.getApplicationNumber();
            enrichApplicationNumber(reg);
            if (reg.getApplicationNumber() != null && !reg.getApplicationNumber().equals(oldVal)) {
                repository.save(reg);
            }
        }
        return list;
    }

    private MarriageRegistration enrichApplicationNumber(MarriageRegistration reg) {
        // ALWAYS try to fetch the most current Application Number from User table
        String foundAppNo = null;

        // Try Groom Phone
        if (reg.getGroomPhone() != null && !reg.getGroomPhone().isEmpty()) {
            foundAppNo = userRepository.findByMobileNumber(reg.getGroomPhone())
                    .map(u -> u.getApplicationNumber()).orElse(null);
        }

        // Try Bride Phone if still null
        if (foundAppNo == null && reg.getBridePhone() != null && !reg.getBridePhone().isEmpty()) {
            foundAppNo = userRepository.findByMobileNumber(reg.getBridePhone())
                    .map(u -> u.getApplicationNumber()).orElse(null);
        }

        if (foundAppNo != null) {
            reg.setApplicationNumber(foundAppNo);
        }
        return reg;
    }

    public MarriageRegistration updateRegistration(Long id, MarriageRegistration updatedData) {
        return repository.findById(id).map(registration -> {
            registration.setApplicationNumber(updatedData.getApplicationNumber());
            // Update Bride Details
            registration.setBrideName(updatedData.getBrideName());
            registration.setBrideInitial(updatedData.getBrideInitial());
            registration.setBrideDob(updatedData.getBrideDob());
            registration.setBrideAge(updatedData.getBrideAge());
            registration.setBrideEmail(updatedData.getBrideEmail());
            registration.setBrideAadhar(updatedData.getBrideAadhar());
            registration.setBrideRegion(updatedData.getBrideRegion());
            registration.setBrideReligion(updatedData.getBrideReligion());
            registration.setBrideCaste(updatedData.getBrideCaste());
            registration.setBrideSubcaste(updatedData.getBrideSubcaste());
            registration.setBrideEducation(updatedData.getBrideEducation());
            registration.setBridePhone(updatedData.getBridePhone());
            registration.setBrideMaritalStatus(updatedData.getBrideMaritalStatus());

            // Update Groom Details
            registration.setGroomName(updatedData.getGroomName());
            registration.setGroomInitial(updatedData.getGroomInitial());
            registration.setGroomDob(updatedData.getGroomDob());
            registration.setGroomAge(updatedData.getGroomAge());
            registration.setGroomEmail(updatedData.getGroomEmail());
            registration.setGroomAadhar(updatedData.getGroomAadhar());
            registration.setGroomRegion(updatedData.getGroomRegion());
            registration.setGroomReligion(updatedData.getGroomReligion());
            registration.setGroomCaste(updatedData.getGroomCaste());
            registration.setGroomSubcaste(updatedData.getGroomSubcaste());
            registration.setGroomEducation(updatedData.getGroomEducation());
            registration.setGroomPhone(updatedData.getGroomPhone());
            registration.setGroomMaritalStatus(updatedData.getGroomMaritalStatus());

            // Update Parents - Bride
            registration.setBrideFatherName(updatedData.getBrideFatherName());
            registration.setBrideFatherDob(updatedData.getBrideFatherDob());
            registration.setBrideFatherAadhar(updatedData.getBrideFatherAadhar());
            registration.setBrideMotherName(updatedData.getBrideMotherName());
            registration.setBrideMotherDob(updatedData.getBrideMotherDob());
            registration.setBrideMotherAadhar(updatedData.getBrideMotherAadhar());

            // Update Parents - Groom
            registration.setGroomFatherName(updatedData.getGroomFatherName());
            registration.setGroomFatherDob(updatedData.getGroomFatherDob());
            registration.setGroomFatherAadhar(updatedData.getGroomFatherAadhar());
            registration.setGroomMotherName(updatedData.getGroomMotherName());
            registration.setGroomMotherDob(updatedData.getGroomMotherDob());
            registration.setGroomMotherAadhar(updatedData.getGroomMotherAadhar());

            // Update Witnesses
            registration.setBrideWitnessName(updatedData.getBrideWitnessName());
            registration.setBrideWitnessRelation(updatedData.getBrideWitnessRelation());
            registration.setBrideWitnessAddress(updatedData.getBrideWitnessAddress());
            registration.setBrideWitnessAadhar(updatedData.getBrideWitnessAadhar());

            registration.setGroomWitnessName(updatedData.getGroomWitnessName());
            registration.setGroomWitnessRelation(updatedData.getGroomWitnessRelation());
            registration.setGroomWitnessAddress(updatedData.getGroomWitnessAddress());
            registration.setGroomWitnessAadhar(updatedData.getGroomWitnessAadhar());

            // Update Marriage Info
            registration.setMarriageDate(updatedData.getMarriageDate());
            registration.setMarriageTime(updatedData.getMarriageTime());
            registration.setReceptionPlace(updatedData.getReceptionPlace());
            registration.setReceptionDate(updatedData.getReceptionDate());

            // Update Addresses
            registration.setBridePermanentAddress(updatedData.getBridePermanentAddress());
            registration.setGroomPermanentAddress(updatedData.getGroomPermanentAddress());

            // Update Status if provided
            if (updatedData.getPaymentStatus() != null) {
                registration.setPaymentStatus(updatedData.getPaymentStatus());
            }

            return repository.save(registration);
        }).orElse(null);
    }

    public void updateStatus(Long id, String status) {
        repository.findById(id).ifPresent(registration -> {
            registration.setPaymentStatus(status);
            repository.save(registration);
        });
    }

    public boolean deleteRegistration(Long id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return true;
        }
        return false;
    }
}
