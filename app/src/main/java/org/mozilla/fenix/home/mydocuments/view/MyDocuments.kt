package org.mozilla.fenix.home.mydocuments.view

import android.text.format.DateUtils
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.max.browser.core.ext.toFileSizeString
import org.mozilla.fenix.R
import org.mozilla.fenix.compose.Divider
import org.mozilla.fenix.compose.EagerFlingBehavior
import org.mozilla.fenix.ext.toComposeColor
import org.mozilla.fenix.home.mydocuments.MyDocumentsItem
import org.mozilla.fenix.home.mydocuments.MyDocumentsItems
import org.mozilla.fenix.theme.FirefoxTheme

private const val MY_DOC_PER_COLUMN = 3

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun MyDocuments(
    myDocumentsItems: MyDocumentsItems,
    backgroundColor: Color = FirefoxTheme.colors.layer2,
    onMyDocumentsItemClick: (MyDocumentsItem, Int) -> Unit,
    onGetPermissionClick: () -> Unit,
) {

    var cardModifier = Modifier.fillMaxWidth()
    if (!myDocumentsItems.hasPermission) {
        cardModifier = cardModifier.clickable { onGetPermissionClick() }
    }

    Card(
        modifier = cardModifier,
        shape = RoundedCornerShape(8.dp),
        backgroundColor = backgroundColor,
        elevation = 6.dp,
    ) {
        if (myDocumentsItems.hasPermission) {
            MyDocumentsList(myDocumentsItems.items) { myDocumentsItem, position ->
                onMyDocumentsItemClick(myDocumentsItem, position)
            }

        } else {
            MyDocumentsGetPermissionView {
                onGetPermissionClick()
            }
        }
    }
}


@Suppress("LongParameterList")
@Composable
private fun MyDocumentsList(
    myDocumentsItems: List<MyDocumentsItem>,
    onMyDocumentsItemClick: (myDocumentsItem: MyDocumentsItem, position: Int) -> Unit,
) {
    val listState = rememberLazyListState()
    val flingBehavior = EagerFlingBehavior(lazyRowState = listState)

    LazyRow(
        state = listState,
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(32.dp),
        flingBehavior = flingBehavior,
    ) {
        val itemsList = myDocumentsItems.chunked(MY_DOC_PER_COLUMN)
        itemsIndexed(itemsList) { pageIndex, items ->
            Column(
                modifier = Modifier.fillMaxWidth(),
            ) {
                items.forEachIndexed { index, myDocumentsItem ->
                    MyDocumentsItem(
                        myDocumentsItem = myDocumentsItem,
                        showDividerLine = index < items.size - 1,
                        onMyDocumentsItemClick = {
                            onMyDocumentsItemClick(myDocumentsItem, index)
                        },
                    )
                }
            }
        }
    }
}


@Suppress("LongParameterList")
@Composable
private fun MyDocumentsItem(
    myDocumentsItem: MyDocumentsItem,
    showDividerLine: Boolean,
    onMyDocumentsItemClick: (myDocumentsItem: MyDocumentsItem) -> Unit,
) {
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val screenWidth = configuration.screenWidthDp.dp

    Row(
        modifier = Modifier
            .clickable { onMyDocumentsItemClick(myDocumentsItem) }
            .size(screenWidth * 0.8f, 56.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(R.drawable.max_ic_pdf),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.fillMaxSize(),
        ) {

            Text(
                text = myDocumentsItem.fileName,
                modifier = Modifier
                    .weight(0.5f)
                    .padding(top = 7.dp),
                color = FirefoxTheme.colors.textPrimary,
                fontSize = 16.sp,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
            )
            Row(
                modifier = Modifier
                    .weight(0.5f)
                    .padding(top = 3.dp),
            ) {
                MyDocumentsItemSubText(
                    text = "${DateUtils.getRelativeTimeSpanString(myDocumentsItem.lastModified)}",
                    modifier = Modifier.weight(0.30f),
                )
                MyDocumentsItemSubText(
                    text = "|",
                    modifier = Modifier.weight(0.05f),
                )
                MyDocumentsItemSubText(
                    text = "${myDocumentsItem.size.toFileSizeString()}",
                    modifier = Modifier.weight(0.5f),
                )
            }

            if (showDividerLine) {
                Divider()
            }
        }
    }
}

@Composable
private fun MyDocumentsItemSubText(text: String, modifier: Modifier) {
    Text(
        text = text,
        modifier = modifier,
        color = when (isSystemInDarkTheme()) {
            true -> FirefoxTheme.colors.textPrimary
            false -> FirefoxTheme.colors.textSecondary
        },
        fontSize = 12.sp,
        overflow = TextOverflow.Ellipsis,
        maxLines = 1,
    )
}


@Preview(locale = "es")
@Composable
private fun MyDocumentsGetPermissionView(
    onGetPermissionClick: () -> Unit,
) {
    FirefoxTheme {

        ConstraintLayout(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .height(70.dp)
                .fillMaxWidth(),
        ) {
            // Create references for the composables to constrain
            val (button, text) = createRefs()

            // 按鈕
            Box(
                modifier = Modifier
                    .background(
                        color = "#5bcdd3".toComposeColor(),
                        shape = RoundedCornerShape(6.dp),
                    )
                    .clickable {
                        onGetPermissionClick()
                    }
                    .constrainAs(button) {
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                        end.linkTo(parent.end)
                    },
            ) {
                Box(
                    modifier = Modifier
                        // .size(97.dp, 36.dp)
                        .wrapContentHeight(Alignment.CenterVertically)
                        .wrapContentWidth(Alignment.CenterHorizontally)
                        .padding(horizontal = 18.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(R.string.max_access),
                        color = Color.White,
                        fontSize = 16.sp,
                        modifier = Modifier,
                    )
                }
            }

            // 描述
            Text(
                modifier = Modifier
                    .padding(end = 16.dp)
                    .constrainAs(text) {
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                        end.linkTo(button.start)
                        start.linkTo(parent.start)
                        width = Dimension.fillToConstraints
                    },
                text = stringResource(R.string.max_allow_to_access_media_file),
                color = when (isSystemInDarkTheme()) {
                    true -> FirefoxTheme.colors.textPrimary
                    false -> FirefoxTheme.colors.textSecondary
                },
                fontSize = 16.sp,
                overflow = TextOverflow.Ellipsis,
                maxLines = 2,
            )
        }
    }
}
