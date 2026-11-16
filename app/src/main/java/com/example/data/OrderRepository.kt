package com.example.data

import kotlinx.coroutines.flow.Flow

class OrderRepository(private val orderDao: OrderDao) {
    val allOrders: Flow<List<OrderEntity>> = orderDao.getAllOrders()

    suspend fun insert(order: OrderEntity) = orderDao.insertOrder(order)
    suspend fun update(order: OrderEntity) = orderDao.updateOrder(order)
    suspend fun delete(order: OrderEntity) = orderDao.deleteOrder(order)
}
