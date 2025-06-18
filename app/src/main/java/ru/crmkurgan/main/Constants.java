package ru.crmkurgan.main;

import androidx.annotation.NonNull;

public interface Constants {
    @NonNull
    String BASEURL = "https://crmkurgan.ru/smart";
    @NonNull
    String CONNECTION = BASEURL + "/api.php?";
    @NonNull
    String SIGNING = CONNECTION + "userdata";
    @NonNull
    String UID = "uid";
    @NonNull
    String NAME = "name";
    @NonNull
    String u_pic = "u_pic";
    @NonNull
    String AGENT = "agent";
    @NonNull
    String LOGIN = "login";
    @NonNull
    String RAND = "rand";
    @NonNull
    String CODE = "code";
    @NonNull
    String MSG = "msg";
    @NonNull
    String PASSWORD = "password";
    @NonNull
    String PREFERENCES = "preferences";
    @NonNull
    String SIGNINGS = CONNECTION + "signup";
    @NonNull
    String VERIFY = CONNECTION + "verify";
    @NonNull
    String WELCOME = "welcome";
    @NonNull
    String SKIPPING = "skipping";
    @NonNull
    String IMAGES = CONNECTION + "images";
    @NonNull
    String IMAGE = "image";
    @NonNull
    String DEFAULT = "https://crmkurgan.ru/izya1.jpg";
    @NonNull
    String NOTIFICATIONS = CONNECTION + "notifications";
    @NonNull
    String NOTIFICATION = "notification";
    @NonNull
    String DATE = "date";
    @NonNull
    String NEW = "new";
    @NonNull
    String DELETE_NOTIFICATIONS = CONNECTION + "delete_notifications";
    @NonNull
    String ID = "id";
    @NonNull
    String DEFAULT_NOTIFICATION_CHANNEL_ID = "/topics/my-app";
    @NonNull
    String DEVICE_TOKEN="device_token";
    @NonNull
    String SAVE_TOKEN = CONNECTION + "save_token";
    @NonNull
    String TITLE = "title";
    @NonNull
    String MESSAGE = "message";
    @NonNull
    String FROM = "from";
    @NonNull
    String ALL = "all";
    @NonNull
    String SAVE_TO_SERVER = CONNECTION + "save_to_server";
    @NonNull
    String GROUP_WORK = CONNECTION + "group_work";
    @NonNull
    String SERVICE = "service";
    @NonNull
    String FCM = "FCM";
    @NonNull
    String HUAWEI = "HUAWEI";
    @NonNull
    String PHONE = "phone";
    @NonNull
    String COUNTRY_CODE = "+7";
    @NonNull
    String TABLE = "table";
    @NonNull
    String PROPERTY_DETAIL = CONNECTION + "property_detail";
    @NonNull
    String ADDRESS = "address";
    @NonNull
    String GALLERY_IMAGE = "gallery_image";
    @NonNull
    String GALLERY = "gallery";
    @NonNull
    String FEATURED = "featured";
    @NonNull
    String PRICE = "price";
    @NonNull
    String COMMENTS = "comments";
    @NonNull
    String ROOM_SIZE = "roomSize";
    @NonNull
    String TOTAL_AREA = "totalArea";
    @NonNull
    String TYPE_OF_LAYOUT = "typeOfLayout";
    @NonNull
    String REPAIR = "repair";
    @NonNull
    String BATHROOM = "bathroom";
    @NonNull
    String BALCONY = "balcony";
    @NonNull
    String FLOORS = "floors";
    @NonNull
    String YEAR_OF_CONSTRUCTION = "yearOfConstruction";
    @NonNull
    String WALL_MATERIALS = "wallMaterials";
    @NonNull
    String AREA_HOUSE = "areaHouse";
    @NonNull
    String NUMBER_OF_ROOMS = "numberOfRooms";
    @NonNull
    String NUMBER_OF_FLOORS = "numberOfFloors";
    @NonNull
    String POWER_SUPPLY = "powerSupply";
    @NonNull
    String HEATING = "heating";
    @NonNull
    String GAS_SUPPLY = "gasSupply";
    @NonNull
    String SEWAGE = "sewage";
    @NonNull
    String PLOT_AREA = "plotArea";
    @NonNull
    String SQUARE = "square";
    @NonNull
    String LAT = "lat";
    @NonNull
    String LNG = "lng";
    @NonNull
    String STATIC_MAP = "https://static-maps.yandex.ru/1.x/?l=map&pt=";
    @NonNull
    String SIGN = ",";
    @NonNull
    String ROUND = "round&z=17";
    @NonNull
    String RATING = CONNECTION + "rating";
    @NonNull
    String RATING_VALUE = "rating_value";
    @NonNull
    String RATING_TEXT = "rating_text";
    @NonNull
    String AGENT_RATE = "agentRate";
    @NonNull
    String AGENT_TOTAL_RATES = "agentTotalRates";
    @NonNull
    String REVIEWS = "reviews";
    @NonNull
    String REVIEW = "review";
    @NonNull
    String TOTAL_REVIEWS = "totalReviews";
    @NonNull
    String GET_REVIEWS = CONNECTION + "getReviews";
    @NonNull
    String TOTAL = "total";
    @NonNull
    String PHOTO_AGENT = "photoAgent";
    @NonNull
    String NAME_AGENT = "nameAgent";
    @NonNull
    String RELATED = "related";
    @NonNull
    String TYPE = "type";
    int DATABASE_VERSION = 1;
    @NonNull
    String DATABASE_NAME = "objects";
    @NonNull
    String INTERNAL_ID = "INTERNAL_ID";
    @NonNull
    String INTERNAL_TABLE = "INTERNAL_TABLE";
    @NonNull
    String CREATE_DATABASE = "CREATE TABLE IF NOT EXISTS " +
            DATABASE_NAME + " (" +
            "_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
            INTERNAL_ID + " TEXT, " +
            INTERNAL_TABLE + " TEXT" + ")";
    @NonNull
    String DROP_DATABASE = "DROP TABLE IF EXISTS " + DATABASE_NAME;
    @NonNull
    String[] PROJECTION = {
            "_ID",
            INTERNAL_ID,
            INTERNAL_TABLE
    };
    @NonNull
    String SELECTED_QUERY = INTERNAL_ID + "=? and " + INTERNAL_TABLE + "=?";
    @NonNull
    String DELETE_QUERY = INTERNAL_ID + "=?";
    @NonNull
    String NAME_AGENT_STROKE = "nameAgentStroke";
    @NonNull
    String THUMBNAIL = "thumbnail";
    @NonNull
    String POSITION = "position";
    @NonNull
    String SLIDER = CONNECTION + "slider";
    @NonNull
    String URL = "url";
    @NonNull
    String BODY = "body";
    @NonNull
    String ICON = "icon";
    @NonNull
    String SENDER = "sender";
    @NonNull
    String POLICY = CONNECTION + "policy";
    @NonNull
    String TEXT_POLICY = "text_policy";
    @NonNull
    String MIME_TYPE = "text/html";
    @NonNull
    String MIME_TYPE_PLAIN = "text/plain";
    @NonNull
    String ENCODING = "utf8";
    @NonNull
    String VIDEO = "video";
    @NonNull
    String AGENT_DETAIL = CONNECTION + AGENT;
    @NonNull
    String TEL = "tel";
    @NonNull
    String AGENT_LISTING = "agentListing";
    @NonNull
    String CHAT_PERSON = CONNECTION + "chat_person";
    @NonNull
    String READIED = "readied";
    @NonNull
    String TIME = "time";
    @NonNull
    String TYPING = CONNECTION + "typing";
    @NonNull
    String TYPING_OR_NOT = "typing_or_not";
    @NonNull
    String CHANGE_TYPING = CONNECTION + "change_typing";
    @NonNull
    String CHAT_LIST = CONNECTION + "chat_list";
    @NonNull
    String ZONE = "zone";
    @NonNull
    String LAST = "last";
    @NonNull
    String USER_ID = "user_id";
    @NonNull
    String UPDATE_ONLINE = CONNECTION + "update_online";
    @NonNull
    String SEND_MESSAGE = CONNECTION + "send_message";
    @NonNull
    String FILTER = CONNECTION + "filter";
}

