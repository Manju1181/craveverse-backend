package com.craveverse.entity;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String customerName;

    @JsonIgnore
     @ManyToOne(fetch = FetchType.LAZY)
      @JoinColumn(name = "customer_id")
      private Customer customer;


    @Column(nullable = false, length = 10)
    private String phone;

    @Column(nullable = false, length = 500)
    private String address;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(nullable = false)
    private String status = "PLACED";

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(
    mappedBy = "order",
    cascade = CascadeType.ALL,
    orphanRemoval = true
)
private List<OrderItem> items = new ArrayList<>();

    public Order() {
    }

    public Long getId() {
        return id;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }
    public Customer getCustomer() {
    return customer;
}

public void setCustomer(Customer customer) {
    this.customer = customer;
}

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }
    

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
    this.status = status;
}

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public List<OrderItem> getItems() {
    return items;
}

public void setItems(List<OrderItem> items) {
    this.items.clear();

    if (items != null) {
        for (OrderItem item : items) {
            addItem(item);
        }
    }
}

public void addItem(OrderItem item) {
    items.add(item);
    item.setOrder(this);
}
}

