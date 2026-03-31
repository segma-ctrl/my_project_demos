package com.example.demo.reservation.mapper;

import com.example.demo.reservation.entity.Booking;
import com.example.demo.reservation.entity.BookingCleanUpEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;


@Mapper(componentModel = "spring")
public interface BookingConvert {
    @Mapping(source = "id", target = "bookingId")
    @Mapping(target="id",ignore = true)
    BookingCleanUpEntity toBookingCleanUpEntity(Booking booking);
    List<BookingCleanUpEntity> toBookingCleanUpEntityList(List<Booking> bookingList);
}
