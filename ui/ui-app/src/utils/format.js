/**
 * 图片地址处理：数据库中部分商品图片地址带有换行等空白字符，需要 trim
 */
export const formatImage = (url) => (url || '').trim()

/**
 * 价格格式化，保留两位小数，如 ¥2464.00
 */
export const formatPrice = (price) => '¥' + Number(price || 0).toFixed(2)
