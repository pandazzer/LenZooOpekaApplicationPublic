package OpekaLenZooApplication.OpekaLenZooApplication.zooMailing.POJO;

import OpekaLenZooApplication.OpekaLenZooApplication.zooMailing.ENUM.StatusCurator;

import java.io.File;
import java.util.List;

public record CuratorsBookkeeping(File curator, List<BookkeepingExist> bookkeeping, String mailAddress, StatusCurator status) {
}
