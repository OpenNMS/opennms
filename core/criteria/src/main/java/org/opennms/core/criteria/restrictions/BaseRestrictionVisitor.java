package org.opennms.core.criteria.restrictions;

public class BaseRestrictionVisitor implements RestrictionVisitor {

	@Override public void visitNull(final NullRestriction restriction) {}
	@Override public void visitNullComplete(final NullRestriction restriction) {}
	@Override public void visitNotNull(final NotNullRestriction restriction) {}
	@Override public void visitNotNullComplete(final NotNullRestriction restriction) {}
	@Override public void visitEq(final EqRestriction restriction) {}
	@Override public void visitEqComplete(final EqRestriction restriction) {}
	@Override public void visitGt(final GtRestriction restriction) {}
	@Override public void visitGtComplete(final GtRestriction restriction) {}
	@Override public void visitGe(final GeRestriction restriction) {}
	@Override public void visitGeComplete(final GeRestriction restriction) {}
	@Override public void visitLt(final LtRestriction restriction) {}
	@Override public void visitLtComplete(final LtRestriction restriction) {}
	@Override public void visitLe(final LeRestriction restriction) {}
	@Override public void visitLeComplete(final LeRestriction restriction) {}
	@Override public void visitAll(final AllRestriction restriction) {}
	@Override public void visitAllComplete(final AllRestriction restriction) {}
	@Override public void visitAny(final AnyRestriction restriction) {}
	@Override public void visitAnyComplete(final AnyRestriction restriction) {}
	@Override public void visitLike(final LikeRestriction restriction) {}
	@Override public void visitLikeComplete(final LikeRestriction restriction) {}
	@Override public void visitIlike(final IlikeRestriction restriction) {}
	@Override public void visitIlikeComplete(final IlikeRestriction restriction) {}
	@Override public void visitIn(final InRestriction restriction) {}
	@Override public void visitInComplete(final InRestriction restriction) {}
	@Override public void visitNot(final NotRestriction restriction) {}
	@Override public void visitNotComplete(final NotRestriction restriction) {}
	@Override public void visitBetween(final BetweenRestriction restriction) {}
	@Override public void visitBetweenComplete(final BetweenRestriction restriction) {}
	@Override public void visitSql(final SqlRestriction restriction) {}
	@Override public void visitSqlComplete(final SqlRestriction restriction) {}
	@Override public void visitIplike(final IplikeRestriction restriction) {}
	@Override public void visitIplikeComplete(final IplikeRestriction restriction) {}

}
