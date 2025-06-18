package ru.crmkurgan.main.utils

interface IRow {
    class ImageRow(val photo: String) : IRow
    class VideoRow(val video: String) : IRow
}
