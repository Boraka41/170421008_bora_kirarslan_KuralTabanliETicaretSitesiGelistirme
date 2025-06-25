package com.example.e_commerce.service;

import com.example.e_commerce.dto.UserDto;
import com.example.e_commerce.entity.User;
import com.example.e_commerce.repository.UserRepository;
import com.example.e_commerce.repository.OrderHistoryRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final OrderHistoryRepository orderHistoryRepository;

    public UserService(PasswordEncoder passwordEncoder, UserRepository userRepository, OrderHistoryRepository orderHistoryRepository) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.orderHistoryRepository = orderHistoryRepository;
    }

    // Kullanıcı kaydı ve doğrulama tokenı oluşturma
    public User registerNewUser(UserDto userDto) {
        if (userRepository.existsByEmail(userDto.getEmail())) {
            throw new RuntimeException("Bu e-posta zaten kayıtlı!");
        }
        User user = new User();
        user.setFullName(userDto.getFullName());
        user.setUsername(userDto.getUsername());
        user.setEmail(userDto.getEmail());
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        user.setRole("USER");
        user.setAge(userDto.getAge());
        user.setGender(userDto.getGender());

        // Email doğrulama için hesap başlangıçta pasif
        user.setEnabled(false);

        // Benzersiz token üretimi
        user.setVerificationToken(UUID.randomUUID().toString());

        // Token geçerlilik süresi (ör: 24 saat)
        user.setTokenExpiration(LocalDateTime.now().plusHours(24));

        return userRepository.save(user);
    }

    // Email doğrulama işlemi için token kontrolü ve kullanıcı aktifleştirme
    public String verifyUser(String token) {
        Optional<User> optionalUser = userRepository.findByVerificationToken(token);

        if (optionalUser.isEmpty()) {
            return "Geçersiz doğrulama linki.";
        }

        User user = optionalUser.get();

        if (user.isEnabled()) {
            return "Hesap zaten doğrulanmış.";
        }

        if (user.getTokenExpiration().isBefore(LocalDateTime.now())) {
            return "Doğrulama linki süresi dolmuş.";
        }

        user.setEnabled(true);
        user.setVerificationToken(null);
        user.setTokenExpiration(null);
        userRepository.save(user);

        return "Hesabınız doğrulandı, giriş yapabilirsiniz.";
    }

    // Diğer mevcut metotlar
    public void deleteOneUser(Long userId) {
        userRepository.deleteById(userId);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getOneUser(Long userId) {
        return userRepository.findById(userId);
    }

    public User createOneUser(User newUser) {
        return userRepository.save(newUser);
    }

    public User updateOneUser(Long userId, User updatedUser) {
        Optional<User> user = userRepository.findById(userId);
        if(user.isPresent()){
            User foundUser = user.get();
            foundUser.setUsername(updatedUser.getUsername());
            foundUser.setEmail(updatedUser.getEmail());
            foundUser.setFullName(updatedUser.getFullName());
            foundUser.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
            return userRepository.save(foundUser);
        }
        else {
            return null;
        }
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> findById(Long userId) {
        return userRepository.findById(userId);
    }

    // Kullanıcı skorunu güncelleme metodu
    public void updateUserScore(String username, Double score) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setScore(score);
            userRepository.save(user);
        }
    }

    // Kullanıcının mevcut skorunu getirme metodu
    public Double getUserScore(String username) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        return userOpt.map(User::getScore).orElse(0.0);
    }

    // Toplam satın alma tutarını güncelleme metodu
    public void updateTotalPurchaseAmount(String username, BigDecimal amount) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setTotalPurchaseAmount(user.getTotalPurchaseAmount().add(amount));
            userRepository.save(user);
        }
    }

    // Kullanıcının mevcut toplam satın alma tutarını getirme metodu
    public BigDecimal getTotalPurchaseAmount(String username) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        return userOpt.map(User::getTotalPurchaseAmount).orElse(BigDecimal.ZERO);
    }

    // Kullanıcının toplam satın alma tutarını veritabanından hesaplayıp güncelleme
    public void recalculateTotalPurchaseAmount(String username) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            BigDecimal totalFromOrders = orderHistoryRepository.sumTotalAmountByUserId(user.getId());
            user.setTotalPurchaseAmount(totalFromOrders);
            userRepository.save(user);
        }
    }

    // Kullanıcının toplam satın alma tutarını yeni sipariş eklediğinde güncelleme
    public void addToPurchaseAmount(String username, BigDecimal amount) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setTotalPurchaseAmount(user.getTotalPurchaseAmount().add(amount));
            userRepository.save(user);
        }
    }

    // Tüm kullanıcıların toplam tutarlarını yeniden hesaplama (initialization için)
    public void recalculateAllUsersPurchaseAmounts() {
        List<User> allUsers = userRepository.findAll();
        
        for (User user : allUsers) {
            try {
                BigDecimal totalFromOrders = orderHistoryRepository.sumTotalAmountByUserId(user.getId());
                user.setTotalPurchaseAmount(totalFromOrders);
                userRepository.save(user);
            } catch (Exception e) {
                // Hata durumunda log yazdırılabilir
            }
        }
    }
}
