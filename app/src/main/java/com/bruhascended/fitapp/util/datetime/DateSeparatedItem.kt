package com.bruhascended.fitapp.util.datetime

import java.util.*

data class DateSeparatedItem <T> (
   val item: T? = null,
   val separator: Date? = null
) {
   override fun equals(other: Any?): Boolean {
      if (this === other) return true
      if (javaClass != other?.javaClass) return false

      other as DateSeparatedItem<*>
      return if (item == null && other.item == null) {
         separator == other.separator
      } else if (item != null && other.item != null) {
         item.hashCode() == other.item.hashCode()
      } else {
         false
      }
   }

   override fun hashCode(): Int {
      var result = item?.hashCode() ?: 0
      result = 31 * result + (separator?.hashCode() ?: 0)
      return result
   }
}