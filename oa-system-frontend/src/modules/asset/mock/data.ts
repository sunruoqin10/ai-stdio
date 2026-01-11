/**
 * 资产管理模块 Mock 数据
 * @module asset/mock
 */

import type { Asset, BorrowRecord, AssetStatistics } from '../types'

/**
 * Mock 资产数据
 */
export const mockAssets: Asset[] = [
  {
    id: 'ASSET000001',
    name: 'MacBook Pro 16英寸',
    category: 'electronic',
    brandModel: 'Apple MacBook Pro M2',
    purchaseDate: '2024-01-15T00:00:00.000Z',
    purchasePrice: 18999,
    currentValue: 16991.09,
    status: 'in_use',
    userId: 'EMP000001',
    userName: '张三',
    userAvatar: 'https://i.pravatar.cc/150?img=1',
    location: 'A座301',
    notes: '研发部使用',
    createdAt: '2024-01-15T00:00:00.000Z',
    updatedAt: '2024-01-15T00:00:00.000Z'
  },
  {
    id: 'ASSET000002',
    name: 'Dell 显示器 27寸',
    category: 'electronic',
    brandModel: 'Dell UltraSharp U2723QE',
    purchaseDate: '2024-02-10T00:00:00.000Z',
    purchasePrice: 3299,
    currentValue: 3062.07,
    status: 'stock',
    location: '仓库A-01',
    notes: '备用显示器',
    createdAt: '2024-02-10T00:00:00.000Z',
    updatedAt: '2024-02-10T00:00:00.000Z'
  },
  {
    id: 'ASSET000003',
    name: '人体工学办公椅',
    category: 'furniture',
    brandModel: '保友Ergonor金豪B',
    purchaseDate: '2024-03-05T00:00:00.000Z',
    purchasePrice: 2299,
    currentValue: 2094.09,
    status: 'borrowed',
    userId: 'EMP000002',
    userName: '李四',
    userAvatar: 'https://i.pravatar.cc/150?img=2',
    location: 'B座205',
    borrowDate: '2024-12-01T00:00:00.000Z',
    expectedReturnDate: '2024-12-30T00:00:00.000Z',
    notes: '借用中',
    createdAt: '2024-03-05T00:00:00.000Z',
    updatedAt: '2024-12-01T00:00:00.000Z'
  },
  {
    id: 'ASSET000004',
    name: 'iPhone 15 Pro',
    category: 'electronic',
    brandModel: 'Apple iPhone 15 Pro 256GB',
    purchaseDate: '2024-04-20T00:00:00.000Z',
    purchasePrice: 7999,
    currentValue: 7391.02,
    status: 'stock',
    location: '仓库A-02',
    images: ['https://picsum.photos/300/200?random=1'],
    createdAt: '2024-04-20T00:00:00.000Z',
    updatedAt: '2024-04-20T00:00:00.000Z'
  },
  {
    id: 'ASSET000005',
    name: '《JavaScript高级程序设计》',
    category: 'book',
    brandModel: '第4版',
    purchaseDate: '2024-05-15T00:00:00.000Z',
    purchasePrice: 99,
    currentValue: 94.05,
    status: 'in_use',
    userId: 'EMP000003',
    userName: '王五',
    userAvatar: 'https://i.pravatar.cc/150?img=3',
    location: '图书角',
    notes: '技术书籍',
    createdAt: '2024-05-15T00:00:00.000Z',
    updatedAt: '2024-05-15T00:00:00.000Z'
  },
  {
    id: 'ASSET000006',
    name: 'HP 激光打印机',
    category: 'electronic',
    brandModel: 'HP LaserJet Pro M404dn',
    purchaseDate: '2024-06-10T00:00:00.000Z',
    purchasePrice: 1599,
    currentValue: 1470.08,
    status: 'maintenance',
    location: '维修部',
    notes: '维修中-卡纸故障',
    createdAt: '2024-06-10T00:00:00.000Z',
    updatedAt: '2024-12-10T00:00:00.000Z'
  },
  {
    id: 'ASSET000007',
    name: '会议桌',
    category: 'furniture',
    brandModel: '定制实木会议桌',
    purchaseDate: '2024-07-01T00:00:00.000Z',
    purchasePrice: 5999,
    currentValue: 5579.07,
    status: 'in_use',
    location: 'A座501会议室',
    notes: '大会议室',
    createdAt: '2024-07-01T00:00:00.000Z',
    updatedAt: '2024-07-01T00:00:00.000Z'
  },
  {
    id: 'ASSET000008',
    name: 'Sony 相机',
    category: 'electronic',
    brandModel: 'Sony Alpha 7 IV',
    purchaseDate: '2024-08-15T00:00:00.000Z',
    purchasePrice: 16999,
    currentValue: 15959.04,
    status: 'borrowed',
    userId: 'EMP000004',
    userName: '赵六',
    userAvatar: 'https://i.pravatar.cc/150?img=4',
    location: '市场部',
    borrowDate: '2024-12-05T00:00:00.000Z',
    expectedReturnDate: '2024-12-25T00:00:00.000Z',
    images: ['https://picsum.photos/300/200?random=2'],
    createdAt: '2024-08-15T00:00:00.000Z',
    updatedAt: '2024-12-05T00:00:00.000Z'
  },
  {
    id: 'ASSET000009',
    name: '《Vue.js设计与实现》',
    category: 'book',
    brandModel: '霍春阳',
    purchaseDate: '2024-09-01T00:00:00.000Z',
    purchasePrice: 79,
    currentValue: 75.05,
    status: 'stock',
    location: '图书角',
    notes: '前端开发书籍',
    createdAt: '2024-09-01T00:00:00.000Z',
    updatedAt: '2024-09-01T00:00:00.000Z'
  },
  {
    id: 'ASSET000010',
    name: 'ThinkPad X1 Carbon',
    category: 'electronic',
    brandModel: 'Lenovo ThinkPad X1 Carbon Gen 11',
    purchaseDate: '2024-10-10T00:00:00.000Z',
    purchasePrice: 12999,
    currentValue: 12349.05,
    status: 'in_use',
    userId: 'EMP000005',
    userName: '孙七',
    userAvatar: 'https://i.pravatar.cc/150?img=5',
    location: 'C座102',
    notes: '销售部使用',
    createdAt: '2024-10-10T00:00:00.000Z',
    updatedAt: '2024-10-10T00:00:00.000Z'
  },
  {
    id: 'ASSET000011',
    name: '文件柜',
    category: 'furniture',
    brandModel: '震旦四门文件柜',
    purchaseDate: '2024-11-05T00:00:00.000Z',
    purchasePrice: 599,
    currentValue: 576.03,
    status: 'stock',
    location: '仓库B-03',
    notes: '新品入库',
    createdAt: '2024-11-05T00:00:00.000Z',
    updatedAt: '2024-11-05T00:00:00.000Z'
  },
  {
    id: 'ASSET000012',
    name: '投影仪',
    category: 'electronic',
    brandModel: 'Epson CB-X49',
    purchaseDate: '2023-12-15T00:00:00.000Z',
    purchasePrice: 4299,
    currentValue: 3589.11,
    status: 'scrapped',
    location: '报废区',
    notes: '灯泡损坏无法修复',
    createdAt: '2023-12-15T00:00:00.000Z',
    updatedAt: '2024-12-01T00:00:00.000Z'
  }
]

/**
 * Mock 借还记录
 */
export const mockBorrowRecords: BorrowRecord[] = [
  {
    id: 'BR000001',
    assetId: 'ASSET000003',
    assetName: '人体工学办公椅',
    borrowerId: 'EMP000002',
    borrowerName: '李四',
    borrowDate: '2024-12-01T00:00:00.000Z',
    expectedReturnDate: '2024-12-30T00:00:00.000Z',
    status: 'active',
    notes: '临时借用',
    createdAt: '2024-12-01T00:00:00.000Z',
    updatedAt: '2024-12-01T00:00:00.000Z'
  },
  {
    id: 'BR000002',
    assetId: 'ASSET000008',
    assetName: 'Sony 相机',
    borrowerId: 'EMP000004',
    borrowerName: '赵六',
    borrowDate: '2024-12-05T00:00:00.000Z',
    expectedReturnDate: '2024-12-25T00:00:00.000Z',
    status: 'active',
    notes: '产品拍摄',
    createdAt: '2024-12-05T00:00:00.000Z',
    updatedAt: '2024-12-05T00:00:00.000Z'
  }
]

/**
 * Mock 统计数据
 */
export const mockStatistics: AssetStatistics = {
  total: 12,
  byCategory: {
    electronic: 7,
    furniture: 3,
    book: 2,
    other: 0
  },
  byStatus: {
    stock: 3,
    in_use: 4,
    borrowed: 2,
    maintenance: 1,
    scrapped: 1
  },
  totalValue: 73170,
  currentValue: 68942.67,
  borrowedCount: 2,
  maintenanceCount: 1
}
