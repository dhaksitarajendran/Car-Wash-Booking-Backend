package com.shineride.catalog.config;

import com.shineride.catalog.entity.ServiceEntity;
import com.shineride.catalog.entity.ServiceType;
import com.shineride.catalog.repository.ServiceRepository;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CatalogSeeder {

    @Bean
    public ApplicationRunner seedCoreServices(ServiceRepository serviceRepository) {
        return args -> {
            upsertCore(serviceRepository, ServiceType.BASIC,
                    "Basic Service",
                    "Exterior wash + dry",
                    250);

            upsertCore(serviceRepository, ServiceType.MODERATE,
                    "Moderate Service",
                    "Exterior + interior cleaning",
                    450);

            upsertCore(serviceRepository, ServiceType.PREMIUM,
                    "Premium Service",
                    "Full detailing + polish",
                    799);
        };
    }

    private void upsertCore(ServiceRepository repo, ServiceType type,
                            String name, String desc, double price) {

        String id = type.name(); // BASIC/MODERATE/PREMIUM

        repo.findById(id).orElseGet(() -> {
            ServiceEntity s = new ServiceEntity();
            s.setId(id);
            s.setServiceType(type);
            s.setServiceName(name);
            s.setServiceDescription(desc);
            s.setBasePrice(price);
            s.setActive(true);
            return repo.save(s);
        });
    }
}