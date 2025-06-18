package ru.crmkurgan.main.utils

import kotlin.math.abs

object ScanUtils
{
	@JvmStatic
	fun compareFloats(left: Double, right: Double): Boolean
	{
		val epsilon = 0.00000001
		return abs(left - right) < epsilon
	}
}